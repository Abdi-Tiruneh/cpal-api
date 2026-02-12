package com.commercepal.apiservice.shared.exceptions.security;

import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;

/**
 * Exception thrown when user is forbidden from accessing a resource.
 */
public class ForbiddenException extends BaseECommerceException {

  private static final String ERROR_CODE = "FORBIDDEN";
  private static final String DEFAULT_MESSAGE = "Access to this resource is forbidden";

  public ForbiddenException() {
    super(ERROR_CODE, DEFAULT_MESSAGE);
  }

  public ForbiddenException(String customMessage) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage);
  }

  public ForbiddenException(String customMessage, Throwable cause) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage, cause);
  }
}

