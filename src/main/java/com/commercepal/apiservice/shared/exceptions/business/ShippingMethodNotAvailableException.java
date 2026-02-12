package com.commercepal.apiservice.shared.exceptions.business;

import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;

/**
 * Exception thrown when requested shipping method is not available.
 */
public class ShippingMethodNotAvailableException extends BaseECommerceException {

  private static final String ERROR_CODE = "SHIPPING_METHOD_NOT_AVAILABLE";
  private static final String DEFAULT_MESSAGE = "Shipping method is not available";

  public ShippingMethodNotAvailableException() {
    super(ERROR_CODE, DEFAULT_MESSAGE);
  }

  public ShippingMethodNotAvailableException(String shippingMethod) {
    super(ERROR_CODE, DEFAULT_MESSAGE,
        String.format("Shipping method '%s' is not available", shippingMethod));
  }

  public ShippingMethodNotAvailableException(String shippingMethod, String reason) {
    super(ERROR_CODE, DEFAULT_MESSAGE,
        String.format("Shipping method '%s' is not available: %s", shippingMethod, reason));
  }
}

