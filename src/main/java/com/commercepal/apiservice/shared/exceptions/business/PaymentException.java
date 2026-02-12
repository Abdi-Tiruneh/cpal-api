package com.commercepal.apiservice.shared.exceptions.business;

import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;

/**
 * Exception thrown when payment-related business rules are violated. Examples: Payment method not
 * supported, payment limit exceeded, invalid payment configuration.
 */
public class PaymentException extends BaseECommerceException {

  private static final String ERROR_CODE = "PAYMENT_ERROR";
  private static final String DEFAULT_MESSAGE = "Payment operation failed";

  public PaymentException() {
    super(ERROR_CODE, DEFAULT_MESSAGE);
  }

  public PaymentException(String customMessage) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage);
  }

  public PaymentException(String customMessage, Throwable cause) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage, cause);
  }
}

