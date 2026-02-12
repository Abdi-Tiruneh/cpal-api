package com.commercepal.apiservice.shared.exceptions.service;

import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;

/**
 * Exception thrown when shipping service operations fail. Examples: Shipping provider errors,
 * tracking service failures, label generation failures.
 */
public class ShippingServiceException extends BaseECommerceException {

  private static final String ERROR_CODE = "SHIPPING_SERVICE_ERROR";
  private static final String DEFAULT_MESSAGE = "Shipping service operation failed";

  public ShippingServiceException() {
    super(ERROR_CODE, DEFAULT_MESSAGE);
  }

  public ShippingServiceException(String customMessage) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage);
  }

  public ShippingServiceException(String customMessage, Throwable cause) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage, cause);
  }
}

