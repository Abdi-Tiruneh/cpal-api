package com.commercepal.apiservice.shared.exceptions.resource;

import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;

/**
 * Exception thrown when attempting to create a resource that already exists.
 */
public class DuplicateResourceException extends BaseECommerceException {

  private static final String ERROR_CODE = "DUPLICATE_RESOURCE";
  private static final String DEFAULT_MESSAGE = "Resource already exists";

  public DuplicateResourceException() {
    super(ERROR_CODE, DEFAULT_MESSAGE);
  }

  public DuplicateResourceException(String customMessage) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage);
  }

  public DuplicateResourceException(String resourceType, String identifier) {
    super(ERROR_CODE, DEFAULT_MESSAGE,
        String.format("%s with identifier '%s' already exists", resourceType, identifier));
  }
}
