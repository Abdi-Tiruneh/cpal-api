package com.commercepal.apiservice.shared.exceptions.business;

import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;

/**
 * Exception thrown when customer purchase limit is exceeded.
 */
public class PurchaseLimitExceededException extends BaseECommerceException {

  private static final String ERROR_CODE = "PURCHASE_LIMIT_EXCEEDED";
  private static final String DEFAULT_MESSAGE = "Purchase limit exceeded";

  public PurchaseLimitExceededException() {
    super(ERROR_CODE, DEFAULT_MESSAGE);
  }

  public PurchaseLimitExceededException(String limitType) {
    super(ERROR_CODE, DEFAULT_MESSAGE,
        String.format("Purchase exceeds %s limit", limitType));
  }

  public PurchaseLimitExceededException(String limitType, Double amount, Double limit) {
    super(ERROR_CODE, DEFAULT_MESSAGE,
        String.format("Purchase amount %.2f exceeds %s limit of %.2f", amount, limitType, limit));
  }
}

