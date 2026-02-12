package com.commercepal.apiservice.shared.exceptions.business;

import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;

/**
 * Exception thrown when attempting to use an expired coupon.
 */
public class CouponExpiredException extends BaseECommerceException {

  private static final String ERROR_CODE = "COUPON_EXPIRED";
  private static final String DEFAULT_MESSAGE = "Coupon code has expired";

  public CouponExpiredException() {
    super(ERROR_CODE, DEFAULT_MESSAGE);
  }

  public CouponExpiredException(String couponCode) {
    super(ERROR_CODE, DEFAULT_MESSAGE,
        String.format("Coupon code '%s' has expired", couponCode));
  }
}

