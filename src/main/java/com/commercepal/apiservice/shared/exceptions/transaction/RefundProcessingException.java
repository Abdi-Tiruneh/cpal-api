package com.commercepal.apiservice.shared.exceptions.transaction;

import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;

/**
 * Exception thrown when refund processing fails.
 */
public class RefundProcessingException extends BaseECommerceException {

  private static final String ERROR_CODE = "REFUND_PROCESSING_ERROR";
  private static final String DEFAULT_MESSAGE = "Refund processing failed";

  public RefundProcessingException() {
    super(ERROR_CODE, DEFAULT_MESSAGE);
  }

  public RefundProcessingException(String customMessage) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage);
  }

  public RefundProcessingException(String customMessage, Throwable cause) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage, cause);
  }
}

