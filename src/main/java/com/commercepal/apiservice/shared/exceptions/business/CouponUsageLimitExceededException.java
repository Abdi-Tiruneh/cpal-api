package com.commercepal.apiservice.shared.exceptions.business;

import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;

/**
 * Exception thrown when coupon usage limit is exceeded.
 */
public class CouponUsageLimitExceededException extends BaseECommerceException {

  private static final String ERROR_CODE = "COUPON_USAGE_LIMIT_EXCEEDED";
  private static final String DEFAULT_MESSAGE = "Coupon usage limit exceeded";

  public CouponUsageLimitExceededException() {
    super(ERROR_CODE, DEFAULT_MESSAGE);
  }

  public CouponUsageLimitExceededException(String couponCode) {
    super(ERROR_CODE, DEFAULT_MESSAGE,
        String.format("Coupon code '%s' has reached its usage limit", couponCode));
  }

  public CouponUsageLimitExceededException(String couponCode, Integer used, Integer limit) {
    super(ERROR_CODE, DEFAULT_MESSAGE,
        String.format("Coupon code '%s' usage limit exceeded. Used: %d, Limit: %d",
            couponCode, used, limit));
  }
}

