package com.commercepal.apiservice.utils.response;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standardized error response structure. Provides comprehensive error information for debugging and
 * user feedback.
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

  /**
   * HTTP status code
   */
  private int status;

  /**
   * Application-specific error code
   */
  private String errorCode;

  /**
   * Human-readable error message
   */
  private String message;

  /**
   * Timestamp when the error occurred
   */
  private LocalDateTime timestamp;

  /**
   * Request path where the error occurred
   */
  private String path;

  /**
   * HTTP method used
   */
  private String method;

  /**
   * Validation errors (for field-specific errors)
   */
  private Map<String, String> validationErrors;

  /**
   * Trace ID for request tracking
   */
  private String traceId;

  /**
   * Create a simple error response
   */
  public static ErrorResponse simple(int status, String errorCode, String message) {
    return ErrorResponse.builder()
        .status(status)
        .errorCode(errorCode)
        .message(message)
        .timestamp(LocalDateTime.now())
        .build();
  }

  /**
   * Create a detailed error response
   */
  public static ErrorResponse detailed(int status, String errorCode, String message, String path,
      String method) {
    return ErrorResponse.builder()
        .status(status)
        .errorCode(errorCode)
        .message(message)
        .path(path)
        .method(method)
        .timestamp(LocalDateTime.now())
        .build();
  }

  /**
   * Create a validation error response
   */
  public static ErrorResponse validation(int status, String errorCode, String message,
      Map<String, String> validationErrors, String path, String method) {
    return ErrorResponse.builder()
        .status(status)
        .errorCode(errorCode)
        .message(message)
        .validationErrors(validationErrors)
        .path(path)
        .method(method)
        .timestamp(LocalDateTime.now())
        .build();
  }
}
