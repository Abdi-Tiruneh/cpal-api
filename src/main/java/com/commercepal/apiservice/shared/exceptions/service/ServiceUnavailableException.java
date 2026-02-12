package com.commercepal.apiservice.shared.exceptions.service;

import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;

/**
 * Exception thrown when a service is temporarily unavailable.
 */
public class ServiceUnavailableException extends BaseECommerceException {

  private static final String ERROR_CODE = "SERVICE_UNAVAILABLE";
  private static final String DEFAULT_MESSAGE = "Service is temporarily unavailable";

  public ServiceUnavailableException() {
    super(ERROR_CODE, DEFAULT_MESSAGE);
  }

  public ServiceUnavailableException(String serviceName) {
    super(ERROR_CODE, DEFAULT_MESSAGE,
        String.format("Service '%s' is temporarily unavailable", serviceName));
  }

  public ServiceUnavailableException(String serviceName, String reason) {
    super(ERROR_CODE, DEFAULT_MESSAGE,
        String.format("Service '%s' is unavailable: %s", serviceName, reason));
  }
}
