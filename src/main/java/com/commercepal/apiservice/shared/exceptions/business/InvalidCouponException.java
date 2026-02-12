package com.commercepal.apiservice.shared.exceptions.business;

import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;

/**
 * Exception thrown when a coupon code is invalid, expired, or not applicable.
 */
public class InvalidCouponException extends BaseECommerceException {

  private static final String ERROR_CODE = "INVALID_COUPON";
  private static final String DEFAULT_MESSAGE = "Coupon code is invalid or not applicable";

  public InvalidCouponException() {
    super(ERROR_CODE, DEFAULT_MESSAGE);
  }

  public InvalidCouponException(String couponCode) {
    super(ERROR_CODE, DEFAULT_MESSAGE,
        String.format("Coupon code '%s' is invalid or not applicable", couponCode));
  }

  public InvalidCouponException(String couponCode, String reason) {
    super(ERROR_CODE, DEFAULT_MESSAGE,
        String.format("Coupon code '%s' is invalid: %s", couponCode, reason));
  }
}

