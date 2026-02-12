package com.commercepal.apiservice.shared.exceptions.business;

import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;

/**
 * Exception thrown when customer-related business rules are violated. Examples: Customer account
 * restrictions, purchase limits, age verification.
 */
public class CustomerException extends BaseECommerceException {

  private static final String ERROR_CODE = "CUSTOMER_ERROR";
  private static final String DEFAULT_MESSAGE = "Customer operation failed";

  public CustomerException() {
    super(ERROR_CODE, DEFAULT_MESSAGE);
  }

  public CustomerException(String customMessage) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage);
  }

  public CustomerException(String customMessage, Throwable cause) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage, cause);
  }
}

