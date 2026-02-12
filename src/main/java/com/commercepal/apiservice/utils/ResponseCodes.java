package com.commercepal.apiservice.utils;

/**
 * Standard response codes for API responses.
 */
public final class ResponseCodes {

  public static final int SUCCESS = 200;
  public static final int BAD_REQUEST = 400;
  public static final int UNAUTHORIZED = 401;
  public static final int FORBIDDEN = 403;
  public static final int NOT_FOUND = 404;
  public static final int INTERNAL_SERVER_ERROR = 500;

  private ResponseCodes() {
    // Utility class - prevent instantiation
  }
}

