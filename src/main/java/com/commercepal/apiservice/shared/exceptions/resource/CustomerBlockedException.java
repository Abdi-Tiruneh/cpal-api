package com.commercepal.apiservice.shared.exceptions.resource;

import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;

/**
 * Exception thrown when a customer account is blocked.
 */
public class CustomerBlockedException extends BaseECommerceException {

  private static final String ERROR_CODE = "CUSTOMER_BLOCKED";
  private static final String DEFAULT_MESSAGE = "Customer account is blocked";

  public CustomerBlockedException() {
    super(ERROR_CODE, DEFAULT_MESSAGE);
  }

  public CustomerBlockedException(String customMessage) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage);
  }

  public CustomerBlockedException(String customerId, String reason) {
    super(ERROR_CODE, DEFAULT_MESSAGE,
        String.format("Customer account '%s' is blocked: %s", customerId, reason));
  }
}
