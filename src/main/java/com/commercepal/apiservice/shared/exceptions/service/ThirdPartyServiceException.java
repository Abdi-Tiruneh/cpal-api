package com.commercepal.apiservice.shared.exceptions.service;

import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;

/**
 * Exception thrown when third-party service operations fail. Examples: External API failures,
 * integration errors, service unavailability.
 */
public class ThirdPartyServiceException extends BaseECommerceException {

  private static final String ERROR_CODE = "THIRD_PARTY_SERVICE_ERROR";
  private static final String DEFAULT_MESSAGE = "Third-party service operation failed";

  public ThirdPartyServiceException() {
    super(ERROR_CODE, DEFAULT_MESSAGE);
  }

  public ThirdPartyServiceException(String customMessage) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage);
  }

  public ThirdPartyServiceException(String customMessage, Throwable cause) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage, cause);
  }

  public ThirdPartyServiceException(String serviceName, String customMessage) {
    super(ERROR_CODE, DEFAULT_MESSAGE,
        String.format("%s service error: %s", serviceName, customMessage));
  }
}
