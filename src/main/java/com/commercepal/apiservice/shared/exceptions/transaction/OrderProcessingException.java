package com.commercepal.apiservice.shared.exceptions.transaction;

import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;

/**
 * Exception thrown when order processing fails. Examples: Order validation failure, order creation
 * failure, order state transition errors.
 */
public class OrderProcessingException extends BaseECommerceException {

  private static final String ERROR_CODE = "ORDER_PROCESSING_ERROR";
  private static final String DEFAULT_MESSAGE = "Order processing failed";

  public OrderProcessingException() {
    super(ERROR_CODE, DEFAULT_MESSAGE);
  }

  public OrderProcessingException(String customMessage) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage);
  }

  public OrderProcessingException(String customMessage, Throwable cause) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage, cause);
  }
}

