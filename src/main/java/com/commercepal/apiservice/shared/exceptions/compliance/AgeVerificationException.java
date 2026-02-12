package com.commercepal.apiservice.shared.exceptions.compliance;

import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;

/**
 * Exception thrown when age verification fails for restricted products.
 */
public class AgeVerificationException extends BaseECommerceException {

  private static final String ERROR_CODE = "AGE_VERIFICATION_FAILED";
  private static final String DEFAULT_MESSAGE = "Age verification failed";

  public AgeVerificationException() {
    super(ERROR_CODE, DEFAULT_MESSAGE);
  }

  public AgeVerificationException(String productName, Integer requiredAge) {
    super(ERROR_CODE, DEFAULT_MESSAGE,
        String.format("Product '%s' requires age verification (minimum age: %d)",
            productName, requiredAge));
  }
}

