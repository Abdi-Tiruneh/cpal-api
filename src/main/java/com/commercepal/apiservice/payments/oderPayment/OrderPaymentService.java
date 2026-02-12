package com.commercepal.apiservice.payments.oderPayment;

import com.commercepal.apiservice.orders.checkout.dto.CheckoutRequest;
import com.commercepal.apiservice.orders.core.model.Order;
import com.commercepal.apiservice.payments.oderPayment.dto.OrderPaymentPageRequest;
import com.commercepal.apiservice.payments.oderPayment.dto.OrderPaymentResponse;
import com.commercepal.apiservice.payments.oderPayment.dto.PaymentInitiationResponse;
import com.commercepal.apiservice.users.customer.Customer;
import org.springframework.data.domain.Page;

public interface OrderPaymentService {

  /**
   * Initialize payment for an order during checkout.
   */
  PaymentInitiationResponse initializePaymentForOrder(
      Order order, Customer customer,
      CheckoutRequest request
  );

  /**
   * Retry a payment transaction with optional payment provider change.
   */
  PaymentInitiationResponse retryPayment(String paymentReference,
      String paymentProviderCode, String paymentProviderVariantCode);

  /**
   * Get order payments with pagination and optional filters (payment reference, order reference).
   */
  Page<OrderPaymentResponse> getOrderPayments(OrderPaymentPageRequest request);
}
