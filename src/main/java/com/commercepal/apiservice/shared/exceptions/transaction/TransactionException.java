package com.commercepal.apiservice.shared.exceptions.transaction;

import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;

/**
 * Base exception for all transaction-related errors.
 */
public class TransactionException extends BaseECommerceException {

  private static final String ERROR_CODE = "TRANSACTION_ERROR";
  private static final String DEFAULT_MESSAGE = "Transaction operation failed";

  public TransactionException() {
    super(ERROR_CODE, DEFAULT_MESSAGE);
  }

  public TransactionException(String customMessage) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage);
  }

  public TransactionException(String customMessage, Throwable cause) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage, cause);
  }
}
