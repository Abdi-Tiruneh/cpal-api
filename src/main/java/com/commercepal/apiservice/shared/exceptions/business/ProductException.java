package com.commercepal.apiservice.shared.exceptions.business;

import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;

/**
 * Exception thrown when product-related business rules are violated. Examples: Product out of
 * stock, product discontinued, invalid product configuration.
 */
public class ProductException extends BaseECommerceException {

  private static final String ERROR_CODE = "PRODUCT_ERROR";
  private static final String DEFAULT_MESSAGE = "Product operation failed";

  public ProductException() {
    super(ERROR_CODE, DEFAULT_MESSAGE);
  }

  public ProductException(String customMessage) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage);
  }

  public ProductException(String customMessage, Throwable cause) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage, cause);
  }
}

