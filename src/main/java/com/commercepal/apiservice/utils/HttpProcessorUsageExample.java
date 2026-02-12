package com.commercepal.apiservice.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

/**
 * Usage examples for HttpProcessor and LongRunningHttpProcessor. This class demonstrates best
 * practices for using the HTTP processors in production.
 *
 * @author CommercePal Engineering
 * @version 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HttpProcessorUsageExample {

  private final HttpProcessor httpProcessor;
  private final LongRunningHttpProcessor longRunningHttpProcessor;

  // ==================== STANDARD HTTP PROCESSOR EXAMPLES ====================

  /**
   * Example 1: Simple async GET request
   */
  public CompletableFuture<String> exampleSimpleGetRequest() {
    String url = "https://api.example.com/users";
    Map<String, String> headers = new HashMap<>();
    headers.put("Authorization", "Bearer token123");

    return httpProcessor.getAsync(url, headers)
        .thenApply(response -> {
          log.info("Received response: {}", response);
          return response;
        })
        .exceptionally(error -> {
          log.error("Request failed: {}", error.getMessage());
          return null;
        });
  }

  /**
   * Example 2: POST request with JSON body
   */
  public CompletableFuture<JSONObject> examplePostJsonRequest() {
    String url = "https://api.example.com/orders";

    JSONObject requestBody = new JSONObject();
    requestBody.put("customerId", "CUST123");
    requestBody.put("amount", 150.00);
    requestBody.put("currency", "USD");

    Map<String, String> headers = new HashMap<>();
    headers.put("Authorization", "Bearer token123");
    headers.put("X-Request-ID", "REQ-" + System.currentTimeMillis());

    return httpProcessor.postJsonAsync(url, requestBody, headers)
        .thenApply(response -> {
          log.info("Order created: {}", response.toString());
          return response;
        });
  }

  /**
   * Example 3: Structured request with detailed response
   */
  public CompletableFuture<JSONObject> exampleStructuredRequest() {
    String url = "https://api.example.com/payments";

    JSONObject payload = new JSONObject();
    payload.put("orderId", "ORD123");
    payload.put("paymentMethod", "CARD");

    return httpProcessor.executeStructuredRequest(
        url,
        "POST",
        payload.toString(),
        Map.of("Content-Type", "application/json")
    ).thenApply(response -> {
      String statusCode = response.getString("StatusCode");
      String statusText = response.getString("StatusText");
      String body = response.getString("ResponseBody");

      log.info("Payment processed: status={}, text={}", statusCode, statusText);
      return response;
    });
  }

  /**
   * Example 4: Multiple parallel requests (Non-blocking)
   */
  public CompletableFuture<Map<String, String>> exampleParallelRequests() {
    CompletableFuture<String> request1 = httpProcessor.getAsync(
        "https://api.example.com/users/123", null);

    CompletableFuture<String> request2 = httpProcessor.getAsync(
        "https://api.example.com/orders/456", null);

    CompletableFuture<String> request3 = httpProcessor.getAsync(
        "https://api.example.com/products/789", null);

    // Wait for all requests to complete
    return CompletableFuture.allOf(request1, request2, request3)
        .thenApply(v -> {
          Map<String, String> results = new HashMap<>();
          results.put("user", request1.join());
          results.put("order", request2.join());
          results.put("product", request3.join());
          return results;
        });
  }

  /**
   * Example 5: Chaining multiple requests
   */
  public CompletableFuture<JSONObject> exampleChainedRequests() {
    String userUrl = "https://api.example.com/users/123";

    // First request: Get user
    return httpProcessor.getAsync(userUrl, null)
        .thenCompose(userResponse -> {
          // Parse user data
          JSONObject user = new JSONObject(userResponse);
          String userId = user.getString("id");

          // Second request: Get user's orders
          String ordersUrl = "https://api.example.com/users/" + userId + "/orders";
          return httpProcessor.getAsync(ordersUrl, null);
        })
        .thenApply(ordersResponse -> {
          JSONObject orders = new JSONObject(ordersResponse);
          log.info("Retrieved orders: {}", orders);
          return orders;
        });
  }

  // ==================== LONG-RUNNING HTTP PROCESSOR EXAMPLES ====================

  /**
   * Example 6: Large file upload or data export
   */
  public CompletableFuture<JSONObject> exampleLongRunningUpload() {
    String url = "https://api.example.com/bulk-upload";

    JSONObject largePayload = new JSONObject();
    largePayload.put("operation", "bulk-import");
    largePayload.put("recordCount", 10000);
    // ... add large dataset

    Map<String, String> headers = new HashMap<>();
    headers.put("Authorization", "Bearer token123");
    headers.put("X-Processing-Type", "ASYNC");

    return longRunningHttpProcessor.postJsonAsync(url, largePayload, headers)
        .thenApply(response -> {
          log.info("Bulk upload completed: {}", response);
          return response;
        })
        .exceptionally(error -> {
          log.error("Long-running upload failed: {}", error.getMessage());
          JSONObject errorResponse = new JSONObject();
          errorResponse.put("error", error.getMessage());
          return errorResponse;
        });
  }

  /**
   * Example 7: Report generation (takes minutes)
   */
  public CompletableFuture<JSONObject> exampleReportGeneration() {
    String url = "https://api.example.com/reports/generate";

    JSONObject reportRequest = new JSONObject();
    reportRequest.put("reportType", "ANNUAL_SALES");
    reportRequest.put("year", 2024);
    reportRequest.put("includeDetails", true);

    return longRunningHttpProcessor.executeStructuredRequest(
        url,
        "POST",
        reportRequest.toString(),
        Map.of("Authorization", "Bearer token123")
    ).thenApply(response -> {
      long duration = response.optLong("DurationMs", 0);
      log.info("Report generated in {} seconds", duration / 1000.0);
      return response;
    });
  }

  /**
   * Example 8: Synchronous long-running request (blocking - use sparingly)
   */
  public JSONObject exampleSyncLongRunningRequest() {
    String url = "https://api.example.com/batch-process";

    JSONObject batchRequest = new JSONObject();
    batchRequest.put("batchId", "BATCH-001");
    batchRequest.put("processType", "DATA_TRANSFORMATION");

    log.warn("Executing blocking long-running request...");

    // This will block the thread for up to 5 minutes
    JSONObject response = longRunningHttpProcessor.postJsonSync(
        url,
        batchRequest,
        Map.of("Authorization", "Bearer token123")
    );

    log.info("Blocking request completed");
    return response;
  }

  /**
   * Example 9: Long-running request with timeout monitoring
   */
  public CompletableFuture<JSONObject> exampleMonitoredLongRunningRequest() {
    String url = "https://api.example.com/data-sync";

    JSONObject syncRequest = new JSONObject();
    syncRequest.put("source", "EXTERNAL_DB");
    syncRequest.put("destination", "WAREHOUSE");

    long startTime = System.currentTimeMillis();

    return longRunningHttpProcessor.postJsonAsync(url, syncRequest, null)
        .thenApply(response -> {
          long totalTime = System.currentTimeMillis() - startTime;
          response.put("clientMeasuredDuration", totalTime);

          if (totalTime > 240_000) { // 4 minutes
            log.warn("Request took longer than expected: {}ms", totalTime);
          }

          return response;
        })
        .orTimeout(5, java.util.concurrent.TimeUnit.MINUTES)
        .exceptionally(error -> {
          log.error("Monitored request failed or timed out: {}", error.getMessage());
          JSONObject errorResponse = new JSONObject();
          errorResponse.put("error", "Request timeout or failure");
          errorResponse.put("message", error.getMessage());
          return errorResponse;
        });
  }

  /**
   * Example 10: Combining standard and long-running processors
   */
  public CompletableFuture<JSONObject> exampleHybridProcessing() {
    // Use standard processor for quick API call
    return httpProcessor.getAsync("https://api.example.com/config", null)
        .thenCompose(configResponse -> {
          JSONObject config = new JSONObject(configResponse);
          boolean useLongRunning = config.optBoolean("requiresLongProcessing", false);

          String processUrl = "https://api.example.com/process";
          JSONObject processRequest = new JSONObject();
          processRequest.put("data", "payload");

          // Dynamically choose processor based on configuration
          if (useLongRunning) {
            log.info("Using long-running processor");
            return longRunningHttpProcessor.postJsonAsync(
                processUrl, processRequest, null);
          } else {
            log.info("Using standard processor");
            return httpProcessor.postJsonAsync(
                processUrl, processRequest, null);
          }
        });
  }

  // ==================== REAL-WORLD USE CASES ====================

  /**
   * Example 11: Third-party payment gateway integration
   */
  public CompletableFuture<JSONObject> processPayment(String orderId, double amount) {
    String paymentGatewayUrl = "https://payment-gateway.example.com/v1/charges";

    JSONObject paymentRequest = new JSONObject();
    paymentRequest.put("orderId", orderId);
    paymentRequest.put("amount", amount);
    paymentRequest.put("currency", "USD");
    paymentRequest.put("idempotencyKey", "PAY-" + System.currentTimeMillis());

    Map<String, String> headers = new HashMap<>();
    headers.put("Authorization", "Bearer payment_gateway_token");
    headers.put("X-API-Version", "2024-01-01");

    return httpProcessor.postJsonAsync(paymentGatewayUrl, paymentRequest, headers)
        .thenApply(response -> {
          if (response.optString("status").equals("success")) {
            log.info("Payment processed successfully: orderId={}", orderId);
          } else {
            log.warn("Payment failed: orderId={}, reason={}",
                orderId, response.optString("error"));
          }
          return response;
        });
  }

  /**
   * Example 12: SMS notification via Twilio
   */
  public CompletableFuture<JSONObject> sendSmsNotification(String phoneNumber, String message) {
    String twilioUrl = "https://api.twilio.com/2010-04-01/Accounts/ACCOUNT_SID/Messages.json";

    JSONObject smsRequest = new JSONObject();
    smsRequest.put("To", phoneNumber);
    smsRequest.put("From", "+1234567890");
    smsRequest.put("Body", message);

    Map<String, String> headers = new HashMap<>();
    headers.put("Authorization", "Basic " + "base64EncodedCredentials");

    return httpProcessor.postJsonAsync(twilioUrl, smsRequest, headers)
        .thenApply(response -> {
          String sid = response.optString("sid");
          log.info("SMS sent: sid={}, to={}", sid, phoneNumber);
          return response;
        });
  }

  /**
   * Example 13: Large data export to S3
   */
  public CompletableFuture<JSONObject> exportLargeDataset(String datasetId) {
    String exportUrl = "https://data-service.example.com/export";

    JSONObject exportRequest = new JSONObject();
    exportRequest.put("datasetId", datasetId);
    exportRequest.put("format", "CSV");
    exportRequest.put("compression", "gzip");
    exportRequest.put("destination", "s3://bucket-name/exports/");

    // Use long-running processor for large exports
    return longRunningHttpProcessor.postJsonAsync(exportUrl, exportRequest, null)
        .thenApply(response -> {
          String exportStatus = response.optString("status");
          String downloadUrl = response.optString("downloadUrl");
          log.info("Export completed: status={}, url={}", exportStatus, downloadUrl);
          return response;
        });
  }

  /**
   * Example 14: Webhook notification to external system
   */
  public void sendWebhookNotification(String webhookUrl, JSONObject eventData) {
    Map<String, String> headers = new HashMap<>();
    headers.put("X-Webhook-Signature", "computed_signature");
    headers.put("X-Event-Type", "ORDER_CREATED");

    // Fire and forget - don't wait for response
    httpProcessor.postJsonAsync(webhookUrl, eventData, headers)
        .thenAccept(response -> log.info("Webhook sent successfully"))
        .exceptionally(error -> {
          log.error("Webhook failed: {}", error.getMessage());
          // Could implement retry logic here
          return null;
        });
  }

  /**
   * Example 15: Health check with timeout
   */
  public CompletableFuture<Boolean> checkServiceHealth(String serviceUrl) {
    return httpProcessor.getAsync(serviceUrl + "/health", null)
        .thenApply(response -> {
          JSONObject health = new JSONObject(response);
          return health.optString("status").equals("UP");
        })
        .exceptionally(error -> {
          log.error("Health check failed for {}: {}", serviceUrl, error.getMessage());
          return false;
        })
        .orTimeout(10, java.util.concurrent.TimeUnit.SECONDS);
  }
}

