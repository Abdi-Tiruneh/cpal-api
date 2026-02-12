package com.commercepal.apiservice.shared.exceptions.service;

import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;

/**
 * Exception thrown when notification service operations fail. Examples: Email service errors, SMS
 * service failures, push notification errors.
 */
public class NotificationServiceException extends BaseECommerceException {

  private static final String ERROR_CODE = "NOTIFICATION_SERVICE_ERROR";
  private static final String DEFAULT_MESSAGE = "Notification service operation failed";

  public NotificationServiceException() {
    super(ERROR_CODE, DEFAULT_MESSAGE);
  }

  public NotificationServiceException(String customMessage) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage);
  }

  public NotificationServiceException(String customMessage, Throwable cause) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage, cause);
  }
}

