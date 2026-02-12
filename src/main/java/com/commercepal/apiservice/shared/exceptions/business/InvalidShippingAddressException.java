package com.commercepal.apiservice.shared.exceptions.business;

import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;

/**
 * Exception thrown when shipping address is invalid or cannot be used.
 */
public class InvalidShippingAddressException extends BaseECommerceException {

  private static final String ERROR_CODE = "INVALID_SHIPPING_ADDRESS";
  private static final String DEFAULT_MESSAGE = "Shipping address is invalid";

  public InvalidShippingAddressException() {
    super(ERROR_CODE, DEFAULT_MESSAGE);
  }

  public InvalidShippingAddressException(String customMessage) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage);
  }

  public InvalidShippingAddressException(String address, String reason) {
    super(ERROR_CODE, DEFAULT_MESSAGE,
        String.format("Shipping address '%s' is invalid: %s", address, reason));
  }
}

