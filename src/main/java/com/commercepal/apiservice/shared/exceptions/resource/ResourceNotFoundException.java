package com.commercepal.apiservice.shared.exceptions.resource;

import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;

/**
 * Exception thrown when a requested resource is not found.
 */
public class ResourceNotFoundException extends BaseECommerceException {

  private static final String ERROR_CODE = "RESOURCE_NOT_FOUND";
  private static final String DEFAULT_MESSAGE = "Requested resource not found";

  public ResourceNotFoundException() {
    super(ERROR_CODE, DEFAULT_MESSAGE);
  }

  public ResourceNotFoundException(String customMessage) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage);
  }

  public ResourceNotFoundException(String customMessage, Throwable cause) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage, cause);
  }

  public ResourceNotFoundException(String resourceType, String resourceId) {
    super(ERROR_CODE, DEFAULT_MESSAGE,
        String.format("%s with ID '%s' not found", resourceType, resourceId));
  }
}
