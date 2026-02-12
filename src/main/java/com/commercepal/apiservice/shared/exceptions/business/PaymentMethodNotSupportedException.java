package com.commercepal.apiservice.shared.exceptions.business;

import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;

/**
 * Exception thrown when a payment method is not supported for the transaction.
 */
public class PaymentMethodNotSupportedException extends BaseECommerceException {

  private static final String ERROR_CODE = "PAYMENT_METHOD_NOT_SUPPORTED";
  private static final String DEFAULT_MESSAGE = "Payment method is not supported";

  public PaymentMethodNotSupportedException() {
    super(ERROR_CODE, DEFAULT_MESSAGE);
  }

  public PaymentMethodNotSupportedException(String paymentMethod) {
    super(ERROR_CODE, DEFAULT_MESSAGE,
        String.format("Payment method '%s' is not supported", paymentMethod));
  }

  public PaymentMethodNotSupportedException(String paymentMethod, String reason) {
    super(ERROR_CODE, DEFAULT_MESSAGE,
        String.format("Payment method '%s' is not supported: %s", paymentMethod, reason));
  }
}

