package com.commercepal.apiservice.shared.exceptions.security;

import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;

/**
 * Exception thrown when too many login attempts are made.
 */
public class LoginAttemptsException extends BaseECommerceException {

  private static final String ERROR_CODE = "TOO_MANY_LOGIN_ATTEMPTS";
  private static final String DEFAULT_MESSAGE = "Too many failed login attempts. Account temporarily locked";

  public LoginAttemptsException() {
    super(ERROR_CODE, DEFAULT_MESSAGE);
  }

  public LoginAttemptsException(String customMessage) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage);
  }

  public LoginAttemptsException(String customMessage, Throwable cause) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage, cause);
  }
}

