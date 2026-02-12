package com.commercepal.apiservice.payments.oderPayment;

import com.commercepal.apiservice.orders.checkout.dto.CheckoutRequest;
import com.commercepal.apiservice.orders.core.model.Order;
import com.commercepal.apiservice.orders.enums.OrderStage;
import com.commercepal.apiservice.orders.enums.PaymentStatus;
import com.commercepal.apiservice.payments.integration.amole.AmoleClient;
import com.commercepal.apiservice.payments.integration.cbebirr.CBEBirrClient;
import com.commercepal.apiservice.payments.integration.ebirr.EbirrClient;
import com.commercepal.apiservice.payments.integration.pesapal.PesapalClient;
import com.commercepal.apiservice.payments.integration.sahay.SahayPayClient;
import com.commercepal.apiservice.payments.integration.telebirr.TelebirrUssdClient;
import com.commercepal.apiservice.payments.oderPayment.dto.OrderPaymentPageRequest;
import com.commercepal.apiservice.payments.oderPayment.dto.OrderPaymentResponse;
import com.commercepal.apiservice.payments.oderPayment.dto.PaymentInitiationResponse;
import com.commercepal.apiservice.payments.oderPayment.enums.NextAction;
import com.commercepal.apiservice.shared.exceptions.business.BadRequestException;
import com.commercepal.apiservice.shared.exceptions.business.PaymentMethodNotSupportedException;
import com.commercepal.apiservice.shared.exceptions.resource.ResourceNotFoundException;
import com.commercepal.apiservice.users.customer.Customer;
import com.commercepal.apiservice.utils.ReferenceGeneratorUtils;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderPaymentServiceImpl implements OrderPaymentService {

  private static final String PAYMENT_REF_PREFIX = "CP";

  private final OrderPaymentRepository orderPaymentRepository;
  private final OrderPaymentMapper orderPaymentMapper;
  private final AmoleClient amoleClient;
  private final SahayPayClient sahayPayClient;
  private final EbirrClient ebirrClient;
  private final CBEBirrClient cbeBirrClient;
  private final TelebirrUssdClient telebirrUssdClient;
  private final PesapalClient pesapalClient;

  @Override
  public PaymentInitiationResponse initializePaymentForOrder(
      Order order, Customer customer,
      CheckoutRequest request
  ) {
    if (!order.getCustomer().getId().equals(customer.getId())) {
      log.warn(
          "Customer attempted to pay for order belonging to another customer - orderId={}, customerId={}, orderCustomerId={}",
          order.getId(), customer.getId(), order.getCustomer().getId());
      throw new BadRequestException("This order cannot be paid");
    }

    validateOrderIsPayable(order);

    // Use paymentProviderCode as gateway identifier
    String gateway = request.paymentProviderCode();
    String accountNumber = request.paymentAccount() != null
        ? request.paymentAccount()
        : null;

    BigDecimal amount = order.getTotalAmount();

    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new BadRequestException("Payment amount must be greater than zero");
    }

    String paymentReference = generateUniquePaymentReference();

    OrderPayment orderPayment = OrderPayment.builder()
        .order(order)
        .customer(customer)
        .reference(paymentReference)
        .gateway(gateway)
        .accountNumber(accountNumber)
        .amount(amount)
        .currency(order.getOrderCurrency())
        .status(PaymentStatus.PENDING)
        .initRequestPayload("{}")
        .initRequestedAt(LocalDateTime.now())
        .build();

    OrderPayment savedOrderPayment = orderPaymentRepository.save(orderPayment);

    try {
      PaymentInitiationResponse paymentInitiationResponse = initiatePaymentWithGateway(
          savedOrderPayment, gateway);

      log.info("Initialized payment for order {}: reference={}, amount={}, gateway={}",
          order.getId(), paymentReference, amount, gateway);

      return paymentInitiationResponse;
    } catch (Exception e) {
      log.error("Failed to initialize payment - orderNumber={}, paymentReference={}, gateway={}",
          order.getOrderNumber(), paymentReference, gateway, e);

      return buildFailureResponse(order.getOrderNumber(), paymentReference);
    }
  }

  @Override
  @Transactional
  public PaymentInitiationResponse retryPayment(
      String paymentReference,
      String paymentProviderCode,
      String paymentProviderVariantCode) {
    log.info("Retrying payment - paymentReference={}, provider={}, variant={}",
        paymentReference, paymentProviderCode, paymentProviderVariantCode);

    // Find payment by reference
    OrderPayment orderPayment = orderPaymentRepository.findByReference(paymentReference)
        .orElseThrow(() -> {
          log.error("Payment not found - paymentReference={}", paymentReference);
          return new ResourceNotFoundException("Payment not found");
        });

    String orderNumber = orderPayment.getOrder().getOrderNumber();

    // Validate order is payable
    validateOrderIsPayable(orderPayment.getOrder());

    // Update gateway if new provider is provided
    String gateway = paymentProviderCode != null && !paymentProviderCode.isBlank()
        ? paymentProviderCode
        : orderPayment.getGateway();

    if (gateway == null || gateway.isBlank()) {
      log.error("Payment gateway is required - paymentReference={}", paymentReference);
      throw new BadRequestException("Payment provider code is required for retry");
    }

    // Update payment gateway if changed
    if (paymentProviderCode != null && !paymentProviderCode.isBlank()
        && !gateway.equals(orderPayment.getGateway())) {
      log.info("Updating payment gateway - paymentReference={}, oldGateway={}, newGateway={}",
          paymentReference, orderPayment.getGateway(), gateway);
      orderPayment.setGateway(gateway);
    }

    // Update init requested timestamp
    orderPayment.setInitRequestedAt(LocalDateTime.now());
    orderPayment.setStatus(PaymentStatus.PENDING);
    OrderPayment savedOrderPayment = orderPaymentRepository.save(orderPayment);

    try {
      PaymentInitiationResponse paymentInitiationResponse = initiatePaymentWithGateway(
          savedOrderPayment, gateway);

      log.info(
          "Payment retry initiated successfully - orderNumber={}, paymentReference={}, gateway={}",
          orderNumber, paymentReference, gateway);

      return paymentInitiationResponse;
    } catch (Exception e) {
      log.error("Failed to retry payment - orderNumber={}, paymentReference={}, gateway={}",
          orderNumber, paymentReference, gateway, e);

      return buildFailureResponse(orderNumber, paymentReference);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public Page<OrderPaymentResponse> getOrderPayments(OrderPaymentPageRequest request) {
    var spec = OrderPaymentSpecification.buildSpecification(request);
    var pageable = request.toPageable();
    return orderPaymentRepository.findAll(spec, pageable).map(orderPaymentMapper::toResponse);
  }

  private PaymentInitiationResponse initiatePaymentWithGateway(
      OrderPayment orderPayment, String gateway) {
    return switch (gateway) {
      case "AMOLE" -> amoleClient.pickAndProcess(orderPayment);
      case "SAHAY" -> sahayPayClient.initiateUssdPush(orderPayment);
      case "EBIRR", "KAAFI_EBIRR", "COOPAY_EBIRR" -> ebirrClient.pickAndProcess(orderPayment);
      case "CBE-BIRR", "CBEBIRR" -> cbeBirrClient.pickAndProcess(orderPayment);
      case "TELEBIRR" -> telebirrUssdClient.initiatePayment(orderPayment);
      case "PESAPAL" -> pesapalClient.pickAndProcess(orderPayment);
      default -> {
        log.error("Unsupported payment gateway - gateway={}, paymentReference={}",
            gateway, orderPayment.getReference());
        throw new PaymentMethodNotSupportedException();
      }
    };
  }

  private PaymentInitiationResponse buildFailureResponse(String orderNumber,
      String paymentReference) {
    return PaymentInitiationResponse.builder()
        .success(false)
        .orderNumber(orderNumber)
        .paymentReference(paymentReference)
        .paymentUrl(null)
        .paymentInstructions(
            "Payment processing failed. Please try again or choose another payment method.")
        .nextAction(NextAction.RETRY_PAYMENT)
        .build();
  }

  private String generateUniquePaymentReference() {
    return ReferenceGeneratorUtils.generateUniqueReference(
        PAYMENT_REF_PREFIX,
        orderPaymentRepository::existsByReference
    );
  }

  private void validateOrderIsPayable(Order order) {
    if (order.getCancelledAt() != null) {
      log.warn("Attempted payment on cancelled order - orderId={}", order.getId());
      throw new BadRequestException("This order cannot accept payments");
    }

    if (order.getCompletedAt() != null) {
      log.warn("Attempted payment on completed order - orderId={}", order.getId());
      throw new BadRequestException("This order cannot accept payments");
    }

    if (order.getCurrentStage() == OrderStage.CANCELLED
        || order.getCurrentStage() == OrderStage.FAILED) {
      log.warn("Attempted payment on order in non-payable stage - orderId={}, stage={}",
          order.getId(), order.getCurrentStage());
      throw new BadRequestException("This order cannot accept payments");
    }
  }
}
