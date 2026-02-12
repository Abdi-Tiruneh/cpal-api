package com.commercepal.apiservice.shared.exceptions.business;

import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;

/**
 * Exception thrown when shipping-related operations fail. Examples: Shipping method not available,
 * invalid shipping address, shipping restrictions.
 */
public class ShippingException extends BaseECommerceException {

  private static final String ERROR_CODE = "SHIPPING_ERROR";
  private static final String DEFAULT_MESSAGE = "Shipping operation failed";

  public ShippingException() {
    super(ERROR_CODE, DEFAULT_MESSAGE);
  }

  public ShippingException(String customMessage) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage);
  }

  public ShippingException(String customMessage, Throwable cause) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage, cause);
  }
}

