package com.commercepal.apiservice.utils;

import com.commercepal.apiservice.shared.enums.SupportedCountry;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Utility class for building standardized API responses.
 */
public final class ApiResponse {

  private ApiResponse() {
    // Utility class - prevent instantiation
  }

  /**
   * Builds a JSON success response with the provided data.
   *
   * @param result      the result JSON object
   * @param userCountry the user's country for response formatting
   * @return ResponseEntity with JSON string body
   */
  public static ResponseEntity<String> buildJsonSuccessResponse(JSONObject result,
      SupportedCountry userCountry) {
    return ResponseEntity
        .status(HttpStatus.OK)
        .header("Content-Type", "application/json")
        .body(result.toString());
  }
}

