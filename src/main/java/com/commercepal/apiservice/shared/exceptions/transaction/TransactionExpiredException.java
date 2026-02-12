package com.commercepal.apiservice.shared.exceptions.transaction;

import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;

/**
 * Exception thrown when a transaction has expired.
 */
public class TransactionExpiredException extends BaseECommerceException {

  private static final String ERROR_CODE = "TRANSACTION_EXPIRED";
  private static final String DEFAULT_MESSAGE = "Transaction has expired";

  public TransactionExpiredException() {
    super(ERROR_CODE, DEFAULT_MESSAGE);
  }

  public TransactionExpiredException(String transactionId) {
    super(ERROR_CODE, DEFAULT_MESSAGE,
        String.format("Transaction '%s' has expired", transactionId));
  }
}
