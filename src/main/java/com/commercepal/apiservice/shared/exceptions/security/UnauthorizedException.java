package com.commercepal.apiservice.shared.exceptions.security;

import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;

/**
 * Exception thrown when user is not authorized to perform the requested operation.
 */
public class UnauthorizedException extends BaseECommerceException {

  private static final String ERROR_CODE = "UNAUTHORIZED";
  private static final String DEFAULT_MESSAGE = "Access denied. Authentication required";

  public UnauthorizedException() {
    super(ERROR_CODE, DEFAULT_MESSAGE);
  }

  public UnauthorizedException(String customMessage) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage);
  }

  public UnauthorizedException(String customMessage, Throwable cause) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage, cause);
  }
}

