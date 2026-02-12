package com.commercepal.apiservice.shared.exceptions.compliance;

import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;

/**
 * Base exception for compliance-related errors.
 */
public class ComplianceException extends BaseECommerceException {

  private static final String ERROR_CODE = "COMPLIANCE_VIOLATION";
  private static final String DEFAULT_MESSAGE = "Compliance violation detected";

  public ComplianceException() {
    super(ERROR_CODE, DEFAULT_MESSAGE);
  }

  public ComplianceException(String customMessage) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage);
  }

  public ComplianceException(String customMessage, Throwable cause) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage, cause);
  }
}
