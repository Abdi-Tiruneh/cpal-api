package com.commercepal.apiservice.shared.exceptions.business;

import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;

/**
 * Exception thrown when a request is malformed or contains invalid data.
 */
public class BadRequestException extends BaseECommerceException {

  private static final String ERROR_CODE = "BAD_REQUEST";
  private static final String DEFAULT_MESSAGE = "Invalid request data provided";

  public BadRequestException() {
    super(ERROR_CODE, DEFAULT_MESSAGE);
  }

  public BadRequestException(String customMessage) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage);
  }

  public BadRequestException(String customMessage, Throwable cause) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage, cause);
  }
}
