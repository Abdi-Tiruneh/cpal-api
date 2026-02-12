package com.commercepal.apiservice.shared.exceptions.business;

import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;

/**
 * Exception thrown when attempting to purchase a product that is out of stock.
 */
public class ProductOutOfStockException extends BaseECommerceException {

  private static final String ERROR_CODE = "PRODUCT_OUT_OF_STOCK";
  private static final String DEFAULT_MESSAGE = "Product is currently out of stock";

  public ProductOutOfStockException() {
    super(ERROR_CODE, DEFAULT_MESSAGE);
  }

  public ProductOutOfStockException(String productSku) {
    super(ERROR_CODE, DEFAULT_MESSAGE,
        String.format("Product with SKU '%s' is currently out of stock", productSku));
  }

  public ProductOutOfStockException(String productSku, Integer requestedQuantity,
      Integer availableQuantity) {
    super(ERROR_CODE, DEFAULT_MESSAGE,
        String.format("Insufficient stock for product '%s'. Requested: %d, Available: %d",
            productSku, requestedQuantity, availableQuantity));
  }
}

