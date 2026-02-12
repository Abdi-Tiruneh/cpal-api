package com.commercepal.apiservice.shared.exceptions.business;

import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;

/**
 * Exception thrown when there is insufficient stock to fulfill a request.
 */
public class InsufficientStockException extends BaseECommerceException {

  private static final String ERROR_CODE = "INSUFFICIENT_STOCK";
  private static final String DEFAULT_MESSAGE = "Insufficient stock available";

  public InsufficientStockException() {
    super(ERROR_CODE, DEFAULT_MESSAGE);
  }

  public InsufficientStockException(String productSku) {
    super(ERROR_CODE, DEFAULT_MESSAGE,
        String.format("Insufficient stock for product '%s'", productSku));
  }

  public InsufficientStockException(String productSku, Integer requested, Integer available) {
    super(ERROR_CODE, DEFAULT_MESSAGE,
        String.format("Insufficient stock for product '%s'. Requested: %d, Available: %d",
            productSku, requested, available));
  }
}

