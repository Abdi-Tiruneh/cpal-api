package com.commercepal.apiservice.shared.exceptions.business;

import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;

/**
 * Exception thrown when inventory operations fail. Examples: Insufficient stock, stock reservation
 * failure, inventory sync issues.
 */
public class InventoryException extends BaseECommerceException {

  private static final String ERROR_CODE = "INVENTORY_ERROR";
  private static final String DEFAULT_MESSAGE = "Inventory operation failed";

  public InventoryException() {
    super(ERROR_CODE, DEFAULT_MESSAGE);
  }

  public InventoryException(String customMessage) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage);
  }

  public InventoryException(String customMessage, Throwable cause) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage, cause);
  }
}

