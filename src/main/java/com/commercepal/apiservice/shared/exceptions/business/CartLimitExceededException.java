package com.commercepal.apiservice.shared.exceptions.business;

import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;

/**
 * Exception thrown when cart item limit is exceeded.
 */
public class CartLimitExceededException extends BaseECommerceException {

  private static final String ERROR_CODE = "CART_LIMIT_EXCEEDED";
  private static final String DEFAULT_MESSAGE = "Cart item limit exceeded";

  public CartLimitExceededException() {
    super(ERROR_CODE, DEFAULT_MESSAGE);
  }

  public CartLimitExceededException(Integer maxItems) {
    super(ERROR_CODE, DEFAULT_MESSAGE,
        String.format("Cart cannot contain more than %d items", maxItems));
  }

  public CartLimitExceededException(Integer currentItems, Integer maxItems) {
    super(ERROR_CODE, DEFAULT_MESSAGE,
        String.format("Cart limit exceeded. Current: %d, Maximum: %d", currentItems, maxItems));
  }
}

