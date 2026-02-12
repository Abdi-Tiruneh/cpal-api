package com.commercepal.apiservice.shared.exceptions.business;

import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;

/**
 * Exception thrown when order-related business rules are violated. Examples: Invalid order state
 * transition, order cancellation rules, order modification restrictions.
 */
public class OrderException extends BaseECommerceException {

  private static final String ERROR_CODE = "ORDER_ERROR";
  private static final String DEFAULT_MESSAGE = "Order operation failed";

  public OrderException() {
    super(ERROR_CODE, DEFAULT_MESSAGE);
  }

  public OrderException(String customMessage) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage);
  }

  public OrderException(String customMessage, Throwable cause) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage, cause);
  }
}

