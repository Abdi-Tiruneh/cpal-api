package com.commercepal.apiservice.shared.exceptions.transaction;

import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;

/**
 * Exception thrown when payment authorization fails.
 */
public class PaymentAuthorizationFailedException extends BaseECommerceException {

  private static final String ERROR_CODE = "PAYMENT_AUTHORIZATION_FAILED";
  private static final String DEFAULT_MESSAGE = "Payment authorization failed";

  public PaymentAuthorizationFailedException() {
    super(ERROR_CODE, DEFAULT_MESSAGE);
  }

  public PaymentAuthorizationFailedException(String customMessage) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage);
  }

  public PaymentAuthorizationFailedException(String paymentId, String reason) {
    super(ERROR_CODE, DEFAULT_MESSAGE,
        String.format("Payment authorization failed for payment '%s': %s", paymentId, reason));
  }
}

