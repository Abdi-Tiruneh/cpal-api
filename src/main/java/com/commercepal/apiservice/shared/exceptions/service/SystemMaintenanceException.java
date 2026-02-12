package com.commercepal.apiservice.shared.exceptions.service;

import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;

/**
 * Exception thrown when system is under maintenance.
 */
public class SystemMaintenanceException extends BaseECommerceException {

  private static final String ERROR_CODE = "SYSTEM_MAINTENANCE";
  private static final String DEFAULT_MESSAGE = "System is currently under maintenance";

  public SystemMaintenanceException() {
    super(ERROR_CODE, DEFAULT_MESSAGE);
  }

  public SystemMaintenanceException(String customMessage) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage);
  }

  public SystemMaintenanceException(String customMessage, String estimatedCompletion) {
    super(ERROR_CODE, DEFAULT_MESSAGE,
        String.format("%s. Estimated completion: %s", customMessage, estimatedCompletion));
  }
}
