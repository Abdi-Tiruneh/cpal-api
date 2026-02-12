package com.commercepal.apiservice.orders.checkout.mapper;

import com.commercepal.apiservice.orders.checkout.dto.CheckoutResult;
import com.commercepal.apiservice.orders.checkout.dto.CheckoutResponse;
import com.commercepal.apiservice.orders.core.model.Order;
import com.commercepal.apiservice.payments.oderPayment.OrderPayment;
import com.commercepal.apiservice.payments.oderPayment.enums.NextAction;
import com.commercepal.apiservice.payments.oderPayment.dto.PaymentInitiationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting Order entities to CheckoutResponse DTOs.
 * <p>
 * Provides clean transformation logic to separate domain models from API responses.
 *
 * @author CommercePal
 * @version 1.0
 */
@Slf4j
@Component
public class CheckoutResponseMapper {

  /**
   * Converts a CheckoutResult (order + payment) to a CheckoutResponse DTO.
   *
   * @param result The checkout result containing order and payment
   * @return Complete checkout response with all order and payment details
   */
  public CheckoutResponse toCheckoutResponse(CheckoutResult result) {
    log.debug("Mapping checkout result to response | CheckoutResponseMapper | toCheckoutResponse | orderNumber={}",
        result.order().getOrderNumber());

    Order order = result.order();

    CheckoutResponse response = CheckoutResponse.builder()
        .orderNumber(order.getOrderNumber())
        .platform(order.getPlatform())
        .currency(order.getOrderCurrency())
        .pricingSummary(mapFinancialSummary(order))
        .paymentStatus(order.getPaymentStatus())
        .orderedAt(order.getOrderedAt())
        .paymentInitiation(result.payment())
        .build();

    log.debug("Checkout result mapped to response | CheckoutResponseMapper | toCheckoutResponse | orderNumber={}", order.getOrderNumber());
    return response;
  }

  /**
   * Maps financial summary from order.
   */
  private CheckoutResponse.FinancialSummary mapFinancialSummary(Order order) {
    return CheckoutResponse.FinancialSummary.builder()
        .subtotal(order.getSubtotal())
        .discountAmount(
            order.getDiscountAmount() != null ? order.getDiscountAmount()
                : java.math.BigDecimal.ZERO)
        .deliveryFee(order.getDeliveryFee())
        .additionalCharges(
            order.getAdditionalCharges() != null ? order.getAdditionalCharges()
                : java.math.BigDecimal.ZERO)
        .totalAmount(order.getTotalAmount())
        .currency(order.getOrderCurrency())
        .build();
  }

  /**
   * Maps payment information to PaymentInitiationResponse.
   *
   * @param order   The order entity
   * @param payment The payment entity (may be null if payment initialization failed)
   * @return PaymentInitiationResponse with payment details, or null if no payment
   */
  private PaymentInitiationResponse mapPaymentResponse(Order order, OrderPayment payment) {
    if (payment == null) {
      log.debug("No payment available | CheckoutResponseMapper | mapPaymentResponse | orderNumber={}", order.getOrderNumber());
      return null;
    }

    log.debug("Mapping payment response | CheckoutResponseMapper | mapPaymentResponse | orderNumber={}, reference={}, gateway={}",
        order.getOrderNumber(), payment.getReference(), payment.getGateway());

    // Extract payment URL from init response if available
    String paymentUrl = extractPaymentUrl(payment);

    return PaymentInitiationResponse.builder()
        .success(true)
        .orderNumber(order.getOrderNumber())
        .paymentReference(payment.getReference())
        .paymentUrl(paymentUrl)
        .paymentInstructions(buildPaymentInstructions(payment, paymentUrl))
        .nextAction(determineNextAction(paymentUrl))
        .build();
  }

  /**
   * Extracts payment URL from payment init response. This will be populated when the payment
   * gateway integration is complete and returns a redirect URL.
   */
  private String extractPaymentUrl(OrderPayment payment) {
    // TODO: Parse initResponsePayload for payment URL when gateway integration is complete
    // For now, return null as the gateway integration hasn't been implemented yet
    return null;
  }

  /**
   * Builds payment instructions based on the payment gateway.
   */
  private String buildPaymentInstructions(OrderPayment payment, String paymentUrl) {
    if (paymentUrl != null && !paymentUrl.isBlank()) {
      return "Please complete your payment by visiting the payment URL.";
    }
    return String.format(
        "Payment initiated with reference %s. Please proceed with %s to complete your payment.",
        payment.getReference(), payment.getGateway());
  }

  /**
   * Determines the next action based on payment state.
   */
  private NextAction determineNextAction(String paymentUrl) {
    if (paymentUrl != null && !paymentUrl.isBlank()) {
      return NextAction.REDIRECT_TO_PAYMENT_URL;
    }
    return NextAction.OPEN_ADDITIONAL_INPUT;
  }
}
