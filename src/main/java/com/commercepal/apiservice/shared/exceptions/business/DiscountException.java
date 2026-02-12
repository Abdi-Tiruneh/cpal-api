package com.commercepal.apiservice.shared.exceptions.business;

import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;

/**
 * Exception thrown when discount/coupon operations fail. Examples: Invalid coupon code, coupon
 * expired, discount not applicable.
 */
public class DiscountException extends BaseECommerceException {

  private static final String ERROR_CODE = "DISCOUNT_ERROR";
  private static final String DEFAULT_MESSAGE = "Discount operation failed";

  public DiscountException() {
    super(ERROR_CODE, DEFAULT_MESSAGE);
  }

  public DiscountException(String customMessage) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage);
  }

  public DiscountException(String customMessage, Throwable cause) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage, cause);
  }
}

