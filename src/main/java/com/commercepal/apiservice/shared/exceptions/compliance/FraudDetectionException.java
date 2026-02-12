package com.commercepal.apiservice.shared.exceptions.compliance;

import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;

/**
 * Exception thrown when fraud is detected in a transaction.
 */
public class FraudDetectionException extends BaseECommerceException {

  private static final String ERROR_CODE = "FRAUD_DETECTED";
  private static final String DEFAULT_MESSAGE = "Transaction flagged for potential fraud";

  public FraudDetectionException() {
    super(ERROR_CODE, DEFAULT_MESSAGE);
  }

  public FraudDetectionException(String customMessage) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage);
  }

  public FraudDetectionException(String transactionId, String reason) {
    super(ERROR_CODE, DEFAULT_MESSAGE,
        String.format("Transaction '%s' flagged for fraud: %s", transactionId, reason));
  }
}

