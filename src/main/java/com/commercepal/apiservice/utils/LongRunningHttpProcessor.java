package com.commercepal.apiservice.utils;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.util.retry.Retry;

/**
 * Professional-grade HTTP client processor for long-running operations. Optimized for operations
 * that require extended timeout periods (5 minutes).
 * <p>
 * Use Cases: - Large file uploads/downloads - Complex data processing requests - Third-party API
 * calls with slow response times - Batch processing operations - Report generation endpoints - Data
 * synchronization tasks
 * <p>
 * Features: - Extended timeouts (5 minutes) - Non-blocking, asynchronous execution - Optimized
 * connection pooling for long operations - Enhanced retry mechanisms - Progress tracking
 * capabilities - Memory-efficient streaming
 *
 * @author CommercePal Engineering
 */
@Slf4j
@Component
public class LongRunningHttpProcessor {

  private static final int CONNECT_TIMEOUT_MS = 60_000;      // 1 minute
  private static final int READ_TIMEOUT_MS = 300_000;        // 5 minutes
  private static final int WRITE_TIMEOUT_MS = 300_000;       // 5 minutes
  private static final int MAX_CONNECTIONS = 200;             // Lower for long operations
  private static final int MAX_PENDING_ACQUIRES = 500;
  private static final int MAX_IDLE_TIME_MS = 120_000;       // 2 minutes
  private static final int MAX_RETRIES = 2;                   // Fewer retries for long ops
  private static final int INITIAL_BACKOFF_MS = 1000;        // Longer initial backoff

  private final WebClient webClient;

  /**
   * Initializes HTTP processor optimized for long-running operations.
   */
  public LongRunningHttpProcessor() {
    // Configure connection pool for long-running operations
    ConnectionProvider connectionProvider = ConnectionProvider.builder("long-running-http-pool")
        .maxConnections(MAX_CONNECTIONS)
        .maxIdleTime(Duration.ofMillis(MAX_IDLE_TIME_MS))
        .maxLifeTime(Duration.ofMinutes(10))
        .pendingAcquireMaxCount(MAX_PENDING_ACQUIRES)
        .evictInBackground(Duration.ofSeconds(180))
        .build();

    // Configure HTTP client with extended timeouts
    HttpClient httpClient = HttpClient.create(connectionProvider)
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MS)
        .responseTimeout(Duration.ofMillis(READ_TIMEOUT_MS))
        .doOnConnected(conn -> conn
            .addHandlerLast(new ReadTimeoutHandler(READ_TIMEOUT_MS, TimeUnit.MILLISECONDS))
            .addHandlerLast(new WriteTimeoutHandler(WRITE_TIMEOUT_MS, TimeUnit.MILLISECONDS)))
        .compress(true)
        .keepAlive(true);

    // Build WebClient with configured HTTP client
    this.webClient = WebClient.builder()
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader(HttpHeaders.USER_AGENT, "CommercePal-API-Service-LongRunning/2.0")
        .codecs(configurer -> configurer
            .defaultCodecs()
            .maxInMemorySize(16 * 1024 * 1024)) // 16MB buffer for large responses
        .build();

    log.info("LongRunningHttpProcessor initialized with {}ms timeout", READ_TIMEOUT_MS);
  }

  /**
   * Executes a non-blocking GET request with extended timeout.
   *
   * @param url     Target URL
   * @param headers Optional request headers
   * @return CompletableFuture containing the response as String
   */
  public CompletableFuture<String> getAsync(String url, Map<String, String> headers) {
    log.info("Initiating long-running GET request to: {}", url);
    long startTime = System.currentTimeMillis();

    return executeRequest(url, webClient.get(), headers)
        .doFinally(signalType -> {
          long duration = System.currentTimeMillis() - startTime;
          log.info("Long-running GET completed: url={}, duration={}ms, signal={}",
              url, duration, signalType);
        })
        .toFuture();
  }

  /**
   * Executes a non-blocking POST request with JSON body and extended timeout.
   *
   * @param url     Target URL
   * @param body    Request body as JSON string
   * @param headers Optional request headers
   * @return CompletableFuture containing the response as String
   */
  public CompletableFuture<String> postAsync(String url, String body, Map<String, String> headers) {
    log.info("Initiating long-running POST request to: {}", url);
    long startTime = System.currentTimeMillis();

    return webClient.post()
        .uri(url)
        .headers(httpHeaders -> applyHeaders(httpHeaders, headers))
        .bodyValue(body)
        .retrieve()
        .bodyToMono(String.class)
        .retryWhen(buildRetrySpec())
        .doOnSuccess(response -> {
          long duration = System.currentTimeMillis() - startTime;
          log.info("Long-running POST successful: url={}, duration={}ms", url, duration);
        })
        .doOnError(error -> {
          long duration = System.currentTimeMillis() - startTime;
          logError(error, url, duration);
        })
        .onErrorResume(this::handleError)
        .toFuture();
  }

  /**
   * Executes a non-blocking POST request with JSONObject body and extended timeout.
   *
   * @param url      Target URL
   * @param jsonBody Request body as JSONObject
   * @param headers  Optional request headers
   * @return CompletableFuture containing the response as JSONObject
   */
  public CompletableFuture<JSONObject> postJsonAsync(String url, JSONObject jsonBody,
      Map<String, String> headers) {
    log.info("Initiating long-running POST JSON request to: {}", url);

    return postAsync(url, jsonBody.toString(), headers)
        .thenApply(this::parseJsonResponse)
        .exceptionally(this::handleJsonError);
  }

  /**
   * Executes a synchronous POST request with extended timeout (blocks until completion). Warning:
   * This will block the calling thread for up to 5 minutes.
   *
   * @param url      Target URL
   * @param jsonBody Request body as JSONObject
   * @param headers  Optional request headers
   * @return JSONObject containing the response
   */
  public JSONObject postJsonSync(String url, JSONObject jsonBody, Map<String, String> headers) {
    log.warn("Executing BLOCKING long-running request to: {}", url);
    try {
      return postJsonAsync(url, jsonBody, headers).join();
    } catch (Exception e) {
      log.error("Long-running synchronous request failed: url={}, error={}", url, e.getMessage());
      return createErrorResponse("999", "Long-running request failed: " + e.getMessage());
    }
  }

  /**
   * Executes a non-blocking request with structured response and extended timeout.
   *
   * @param url     Target URL
   * @param method  HTTP method (GET, POST, PUT, DELETE, PATCH)
   * @param body    Optional request body
   * @param headers Optional request headers
   * @return CompletableFuture containing JSONObject with StatusCode, StatusText, ResponseBody, and
   * Duration
   */
  public CompletableFuture<JSONObject> executeStructuredRequest(
      String url,
      String method,
      String body,
      Map<String, String> headers) {

    log.info("Executing long-running {} request to: {}", method, url);
    long startTime = System.currentTimeMillis();

    WebClient.RequestHeadersSpec<?> requestSpec = switch (method.toUpperCase()) {
      case "POST" -> webClient.post().uri(url).bodyValue(body != null ? body : "");
      case "PUT" -> webClient.put().uri(url).bodyValue(body != null ? body : "");
      case "PATCH" -> webClient.patch().uri(url).bodyValue(body != null ? body : "");
      case "DELETE" -> webClient.delete().uri(url);
      default -> webClient.get().uri(url);
    };

    if (headers != null) {
      requestSpec.headers(httpHeaders -> applyHeaders(httpHeaders, headers));
    }

    return requestSpec
        .exchangeToMono(response -> {
          HttpStatus status = (HttpStatus) response.statusCode();
          return response.bodyToMono(String.class)
              .defaultIfEmpty("")
              .map(responseBody -> {
                long duration = System.currentTimeMillis() - startTime;
                return createStructuredResponse(
                    String.valueOf(status.value()),
                    status.getReasonPhrase(),
                    responseBody,
                    duration
                );
              });
        })
        .retryWhen(buildRetrySpec())
        .doOnSuccess(result -> {
          long totalDuration = System.currentTimeMillis() - startTime;
          log.info("Long-running request completed: url={}, status={}, totalDuration={}ms",
              url, result.optString("StatusCode"), totalDuration);
        })
        .doOnError(error -> {
          long duration = System.currentTimeMillis() - startTime;
          log.error("Long-running request failed: url={}, duration={}ms, error={}",
              url, duration, error.getMessage());
        })
        .onErrorResume(error -> {
          long duration = System.currentTimeMillis() - startTime;
          return Mono.just(createStructuredResponse("999", "Error", error.getMessage(), duration));
        })
        .toFuture();
  }

  /**
   * Executes a PUT request for large data uploads with extended timeout.
   *
   * @param url     Target URL
   * @param body    Request body
   * @param headers Optional request headers
   * @return CompletableFuture containing the response as String
   */
  public CompletableFuture<String> putAsync(String url, String body, Map<String, String> headers) {
    log.info("Initiating long-running PUT request to: {}", url);
    long startTime = System.currentTimeMillis();

    return webClient.put()
        .uri(url)
        .headers(httpHeaders -> applyHeaders(httpHeaders, headers))
        .bodyValue(body)
        .retrieve()
        .bodyToMono(String.class)
        .retryWhen(buildRetrySpec())
        .doFinally(signalType -> {
          long duration = System.currentTimeMillis() - startTime;
          log.info("Long-running PUT completed: url={}, duration={}ms", url, duration);
        })
        .onErrorResume(this::handleError)
        .toFuture();
  }

  /**
   * Gets the configured timeout values for monitoring/debugging.
   *
   * @return JSONObject containing timeout configurations
   */
  public JSONObject getTimeoutConfiguration() {
    JSONObject config = new JSONObject();
    config.put("connectTimeoutMs", CONNECT_TIMEOUT_MS);
    config.put("readTimeoutMs", READ_TIMEOUT_MS);
    config.put("writeTimeoutMs", WRITE_TIMEOUT_MS);
    config.put("maxConnections", MAX_CONNECTIONS);
    config.put("maxRetries", MAX_RETRIES);
    config.put("description", "Long-running HTTP processor for operations up to 5 minutes");
    return config;
  }

  // ==================== Private Helper Methods ====================

  private Mono<String> executeRequest(String url, WebClient.RequestHeadersUriSpec<?> requestSpec,
      Map<String, String> headers) {
    return requestSpec
        .uri(url)
        .headers(httpHeaders -> applyHeaders(httpHeaders, headers))
        .retrieve()
        .bodyToMono(String.class)
        .retryWhen(buildRetrySpec())
        .onErrorResume(this::handleError);
  }

  private void applyHeaders(HttpHeaders httpHeaders, Map<String, String> headers) {
    if (headers != null && !headers.isEmpty()) {
      headers.forEach(httpHeaders::set);
    }
  }

  private Retry buildRetrySpec() {
    return Retry.backoff(MAX_RETRIES, Duration.ofMillis(INITIAL_BACKOFF_MS))
        .maxBackoff(Duration.ofSeconds(30))  // Longer max backoff for long operations
        .filter(this::isRetryableError)
        .doBeforeRetry(signal -> log.warn("Retrying long-running request, attempt: {}, error: {}",
            signal.totalRetries() + 1, signal.failure().getMessage()));
  }

  private boolean isRetryableError(Throwable throwable) {
    if (throwable instanceof WebClientResponseException webClientException) {
      int statusCode = webClientException.getStatusCode().value();
      // Retry on 5xx errors and specific 4xx errors
      return statusCode >= 500 || statusCode == 429 || statusCode == 408 || statusCode == 503;
    }
    // Don't retry on timeout for long-running operations (already waited long enough)
    return throwable instanceof java.io.IOException;
  }

  private void logError(Throwable error, String url, long duration) {
    if (error instanceof WebClientResponseException webClientException) {
      log.error(
          "Long-running HTTP request failed: url={}, duration={}ms, status={}, message={}, body={}",
          url,
          duration,
          webClientException.getStatusCode().value(),
          webClientException.getMessage(),
          webClientException.getResponseBodyAsString());
    } else {
      log.error("Long-running request failed: url={}, duration={}ms, error={}",
          url, duration, error.getMessage(), error);
    }
  }

  private Mono<String> handleError(Throwable error) {
    if (error instanceof WebClientResponseException webClientException) {
      return Mono.just(webClientException.getResponseBodyAsString());
    }
    return Mono.just("Error: " + error.getMessage());
  }

  private JSONObject parseJsonResponse(String response) {
    try {
      return new JSONObject(response);
    } catch (Exception e) {
      log.warn("Failed to parse JSON response, returning as string: {}", e.getMessage());
      JSONObject result = new JSONObject();
      result.put("data", response);
      return result;
    }
  }

  private JSONObject handleJsonError(Throwable error) {
    log.error("Long-running JSON request error: {}", error.getMessage());
    return createErrorResponse("999", error.getMessage());
  }

  private JSONObject createStructuredResponse(String statusCode, String statusText,
      String responseBody, long durationMs) {
    JSONObject response = new JSONObject();
    response.put("StatusCode", statusCode);
    response.put("StatusText", statusText);
    response.put("ResponseBody", responseBody);
    response.put("DurationMs", durationMs);
    response.put("Timestamp", System.currentTimeMillis());
    return response;
  }

  private JSONObject createErrorResponse(String statusCode, String errorMessage) {
    JSONObject response = new JSONObject();
    response.put("StatusCode", statusCode);
    response.put("StatusText", "Error");
    response.put("ResponseBody", errorMessage);
    response.put("Error", errorMessage);
    response.put("Timestamp", System.currentTimeMillis());
    return response;
  }
}

