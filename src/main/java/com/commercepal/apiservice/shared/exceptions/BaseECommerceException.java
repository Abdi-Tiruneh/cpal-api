package com.commercepal.apiservice.shared.exceptions;

import lombok.Getter;

/**
 * Base exception class for all e-commerce related exceptions. Provides common functionality and
 * structure for all custom exceptions.
 * <p>
 * This is the foundation for a comprehensive exception hierarchy that enables: - Consistent error
 * handling across the application - Structured error codes for API consumers - Detailed error
 * messages with context - Proper exception chaining for debugging
 */
@Getter
public abstract class BaseECommerceException extends RuntimeException {

  private final String errorCode;
  private final String defaultMessage;

  protected BaseECommerceException(String errorCode, String defaultMessage) {
    super(defaultMessage);
    this.errorCode = errorCode;
    this.defaultMessage = defaultMessage;
  }

  protected BaseECommerceException(String errorCode, String defaultMessage, String customMessage) {
    super(
        customMessage != null && !customMessage.trim().isEmpty() ? customMessage : defaultMessage);
    this.errorCode = errorCode;
    this.defaultMessage = defaultMessage;
  }

  protected BaseECommerceException(String errorCode, String defaultMessage, String customMessage,
      Throwable cause) {
    super(customMessage != null && !customMessage.trim().isEmpty() ? customMessage : defaultMessage,
        cause);
    this.errorCode = errorCode;
    this.defaultMessage = defaultMessage;
  }

}
