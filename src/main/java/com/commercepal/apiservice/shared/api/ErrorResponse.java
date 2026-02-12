package com.commercepal.apiservice.shared.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/**
 * Standard error response structure for the e-commerce API. Provides consistent error format across
 * all endpoints.
 */
@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

  private Integer status;
  private String errorCode;
  private String message;
  private Map<String, String> errors;
  private String path;
  private String method;
  private String traceId;
  private LocalDateTime timestamp;

  /**
   * Creates a detailed error response with all fields.
   */
  public static ErrorResponse detailed(Integer status, String errorCode, String message,
      String path, String method) {
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
   * Creates a validation error response with field-level errors.
   */
  public static ErrorResponse validation(Integer status, String errorCode, String message,
      Map<String, String> errors, String path, String method) {
    return ErrorResponse.builder()
        .status(status)
        .errorCode(errorCode)
        .message(message)
        .errors(errors)
        .path(path)
        .method(method)
        .timestamp(LocalDateTime.now())
        .build();
  }

  /**
   * Creates a simple error response.
   */
  public static ErrorResponse simple(String message, Map<String, String> errors) {
    return ErrorResponse.builder()
        .message(message)
        .errors(errors)
        .timestamp(LocalDateTime.now())
        .build();
  }
}

