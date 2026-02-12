package com.commercepal.apiservice.shared.exceptions.business;

import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;

/**
 * Exception thrown when shopping cart operations fail. Examples: Cart limit exceeded, invalid cart
 * item, cart expired.
 */
public class CartException extends BaseECommerceException {

  private static final String ERROR_CODE = "CART_ERROR";
  private static final String DEFAULT_MESSAGE = "Shopping cart operation failed";

  public CartException() {
    super(ERROR_CODE, DEFAULT_MESSAGE);
  }

  public CartException(String customMessage) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage);
  }

  public CartException(String customMessage, Throwable cause) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage, cause);
  }
}

