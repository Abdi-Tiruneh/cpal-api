package com.commercepal.apiservice.shared.exceptions.business;

import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;

/**
 * Exception thrown when an order cannot be cancelled. Examples: Order already shipped, order
 * already delivered, cancellation deadline passed.
 */
public class OrderCancellationException extends BaseECommerceException {

  private static final String ERROR_CODE = "ORDER_CANCELLATION_FAILED";
  private static final String DEFAULT_MESSAGE = "Order cannot be cancelled";

  public OrderCancellationException() {
    super(ERROR_CODE, DEFAULT_MESSAGE);
  }

  public OrderCancellationException(String customMessage) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage);
  }

  public OrderCancellationException(String orderNumber, String reason) {
    super(ERROR_CODE, DEFAULT_MESSAGE,
        String.format("Order '%s' cannot be cancelled: %s", orderNumber, reason));
  }
}

