package com.commercepal.apiservice.shared.exceptions.transaction;

import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;

/**
 * Exception thrown when attempting to create a duplicate transaction.
 */
public class DuplicateTransactionException extends BaseECommerceException {

  private static final String ERROR_CODE = "DUPLICATE_TRANSACTION";
  private static final String DEFAULT_MESSAGE = "Duplicate transaction detected";

  public DuplicateTransactionException() {
    super(ERROR_CODE, DEFAULT_MESSAGE);
  }

  public DuplicateTransactionException(String transactionId) {
    super(ERROR_CODE, DEFAULT_MESSAGE,
        String.format("Transaction '%s' already exists", transactionId));
  }
}
