package com.commercepal.apiservice.shared.exceptions.transaction;

import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;

/**
 * Exception thrown when payment processing fails. Examples: Payment gateway errors, payment
 * authorization failures, payment capture failures.
 */
public class PaymentProcessingException extends BaseECommerceException {

  private static final String ERROR_CODE = "PAYMENT_PROCESSING_ERROR";
  private static final String DEFAULT_MESSAGE = "Payment processing failed";

  public PaymentProcessingException() {
    super(ERROR_CODE, DEFAULT_MESSAGE);
  }

  public PaymentProcessingException(String customMessage) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage);
  }

  public PaymentProcessingException(String customMessage, Throwable cause) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage, cause);
  }
}
