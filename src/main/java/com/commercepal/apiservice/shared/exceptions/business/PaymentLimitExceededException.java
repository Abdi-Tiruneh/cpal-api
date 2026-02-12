package com.commercepal.apiservice.shared.exceptions.business;

import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;

/**
 * Exception thrown when payment amount exceeds allowed limits.
 */
public class PaymentLimitExceededException extends BaseECommerceException {

  private static final String ERROR_CODE = "PAYMENT_LIMIT_EXCEEDED";
  private static final String DEFAULT_MESSAGE = "Payment amount exceeds allowed limit";

  public PaymentLimitExceededException() {
    super(ERROR_CODE, DEFAULT_MESSAGE);
  }

  public PaymentLimitExceededException(String limitType) {
    super(ERROR_CODE, DEFAULT_MESSAGE,
        String.format("Payment exceeds %s limit", limitType));
  }

  public PaymentLimitExceededException(String limitType, Double amount, Double limit) {
    super(ERROR_CODE, DEFAULT_MESSAGE,
        String.format("Payment amount %.2f exceeds %s limit of %.2f", amount, limitType, limit));
  }
}

