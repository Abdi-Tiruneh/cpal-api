package com.commercepal.apiservice.utils;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
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
 * Professional-grade HTTP client processor with Cloudflare-safe behavior.
 * 
 * Features:
 * - Non-blocking reactive execution
 * - Connection pooling
 * - Cloudflare-compatible headers
 * - Randomized real User-Agent per request
 * - Retry with backoff
 * - All legacy & structured methods preserved
 *
 * @author CommercePal
 * @version 2.1.0
 */
@Slf4j
@Component
public class HttpProcessor {

  private static final int CONNECT_TIMEOUT_MS = 30_000;
  private static final int READ_TIMEOUT_MS = 30_000;
  private static final int WRITE_TIMEOUT_MS = 30_000;
  private static final int MAX_CONNECTIONS = 500;
  private static final int MAX_PENDING_ACQUIRES = 1000;
  private static final int MAX_IDLE_TIME_MS = 45_000;
  private static final int MAX_RETRIES = 3;
  private static final int INITIAL_BACKOFF_MS = 100;

  private static final List<String> USER_AGENTS = List.of(
      "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36",
      "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36",
      "Mozilla/5.0 (Macintosh; Intel Mac OS X 13_5) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Safari/605.1.15",
      "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:122.0) Gecko/20100101 Firefox/122.0",
      "Mozilla/5.0 (X11; Linux x86_64; rv:121.0) Gecko/20100101 Firefox/121.0"
  );

  private final WebClient webClient;

  public HttpProcessor() {
    CookieManager cookieManager = new CookieManager();
    cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

    ConnectionProvider connectionProvider = ConnectionProvider.builder("http-pool")
        .maxConnections(MAX_CONNECTIONS)
        .maxIdleTime(Duration.ofMillis(MAX_IDLE_TIME_MS))
        .maxLifeTime(Duration.ofMinutes(5))
        .pendingAcquireMaxCount(MAX_PENDING_ACQUIRES)
        .evictInBackground(Duration.ofSeconds(120))
        .build();

    HttpClient httpClient = HttpClient.create(connectionProvider)
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MS)
        .responseTimeout(Duration.ofMillis(READ_TIMEOUT_MS))
        .doOnConnected(conn -> conn
            .addHandlerLast(new ReadTimeoutHandler(READ_TIMEOUT_MS, TimeUnit.MILLISECONDS))
            .addHandlerLast(new WriteTimeoutHandler(WRITE_TIMEOUT_MS, TimeUnit.MILLISECONDS)))
        .compress(true)
        .keepAlive(true);

    this.webClient = WebClient.builder()
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .codecs(cfg -> cfg.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        .build();
  }

  // ======================= Public Methods =======================

  public CompletableFuture<String> getAsync(String url, Map<String, String> headers) {
    log.debug("Initiating GET request to: {}", url);
    return executeRequest(url, webClient.get(), headers).toFuture();
  }

  public CompletableFuture<String> postAsync(String url, String body, Map<String, String> headers) {
    log.debug("Initiating POST request to: {}", url);
    return webClient.post()
        .uri(url)
        .headers(h -> applyHeaders(h, headers))
        .bodyValue(body)
        .retrieve()
        .bodyToMono(String.class)
        .retryWhen(buildRetrySpec())
        .doOnSuccess(r -> log.info("POST request successful: url={}", url))
        .doOnError(this::logError)
        .onErrorResume(this::handleError)
        .toFuture();
  }

  public CompletableFuture<JSONObject> postJsonAsync(String url, JSONObject jsonBody, Map<String, String> headers) {
    log.debug("Initiating POST JSON request to: {}", url);
    return postAsync(url, jsonBody.toString(), headers)
        .thenApply(this::parseJsonResponse)
        .exceptionally(this::handleJsonError);
  }

  public JSONObject postJsonSync(String url, JSONObject jsonBody, Map<String, String> headers) {
    try {
      return postJsonAsync(url, jsonBody, headers).join();
    } catch (Exception e) {
      log.error("Synchronous request failed: url={}, error={}", url, e.getMessage());
      return createErrorResponse("999", "Request failed: " + e.getMessage());
    }
  }

  public CompletableFuture<JSONObject> executeStructuredRequest(String url, String method, String body, Map<String, String> headers) {
    log.debug("Executing {} request to: {}", method, url);

    WebClient.RequestHeadersSpec<?> requestSpec = switch (method.toUpperCase()) {
      case "POST" -> webClient.post().uri(url).bodyValue(body != null ? body : "");
      case "PUT" -> webClient.put().uri(url).bodyValue(body != null ? body : "");
      case "PATCH" -> webClient.patch().uri(url).bodyValue(body != null ? body : "");
      case "DELETE" -> webClient.delete().uri(url);
      default -> webClient.get().uri(url);
    };

    if (headers != null) {
      requestSpec.headers(h -> applyHeaders(h, headers));
    }

    return requestSpec
        .exchangeToMono(resp -> {
          // HttpStatusCode is returned now
          var statusCode = resp.statusCode();
          int code = statusCode.value();
          String reason = statusCode.toString(); // or statusCode.getReasonPhrase() if using Spring 6.1+

          return resp.bodyToMono(String.class)
              .defaultIfEmpty("")
              .map(bodyResp -> createStructuredResponse(
                  String.valueOf(code),
                  reason,
                  bodyResp
              ));
        })
        .retryWhen(buildRetrySpec())
        .doOnSuccess(result -> log.info("Request completed: url={}, status={}", url, result.optString("StatusCode")))
        .doOnError(err -> log.error("Request failed: url={}, error={}", url, err.getMessage()))
        .onErrorResume(err -> Mono.just(createErrorResponse("999", err.getMessage())))
        .toFuture();

  }

  @Deprecated(since = "2.0.0", forRemoval = true)
  public String processProperRequest(String url, String body, Map<String, String> headers) {
    try {
      return postAsync(url, body, headers).join();
    } catch (Exception e) {
      log.error("Legacy request failed: {}", e.getMessage());
      return "Error: " + e.getMessage();
    }
  }

  @Deprecated(since = "2.0.0", forRemoval = true)
  public JSONObject jsonRequestProcessor(String url, String body, Map<String, String> headers) {
    return postJsonSync(url, new JSONObject(body), headers);
  }

  // ======================= Private Helpers =======================

  private Mono<String> executeRequest(String url, WebClient.RequestHeadersUriSpec<?> requestSpec, Map<String, String> headers) {
    return requestSpec.uri(url)
        .headers(h -> applyHeaders(h, headers))
        .retrieve()
        .bodyToMono(String.class)
        .retryWhen(buildRetrySpec())
        .doOnSuccess(r -> log.info("Request successful: url={}", url))
        .doOnError(this::logError)
        .onErrorResume(this::handleError);
  }

  private void applyHeaders(HttpHeaders httpHeaders, Map<String, String> headers) {
    httpHeaders.set(HttpHeaders.USER_AGENT, randomUserAgent());
    httpHeaders.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
    httpHeaders.set(HttpHeaders.CONNECTION, "keep-alive");

    if (headers != null && !headers.isEmpty()) {
      headers.forEach(httpHeaders::set);
    }
  }

  private String randomUserAgent() {
    return USER_AGENTS.get(ThreadLocalRandom.current().nextInt(USER_AGENTS.size()));
  }

  private Retry buildRetrySpec() {
    return Retry.backoff(MAX_RETRIES, Duration.ofMillis(INITIAL_BACKOFF_MS))
        .maxBackoff(Duration.ofSeconds(5))
        .filter(this::isRetryableError);
  }

  private boolean isRetryableError(Throwable t) {
    if (t instanceof WebClientResponseException e) {
      int code = e.getStatusCode().value();
      return code >= 500 || code == 429 || code == 408;
    }
    return t instanceof java.io.IOException || t instanceof java.util.concurrent.TimeoutException;
  }

  private void logError(Throwable error) {
    if (error instanceof WebClientResponseException e) {
      log.error("HTTP request failed: status={}, message={}, body={}",
          e.getStatusCode().value(), e.getMessage(), e.getResponseBodyAsString());
    } else {
      log.error("Request failed: error={}", error.getMessage(), error);
    }
  }

  private Mono<String> handleError(Throwable error) {
    if (error instanceof WebClientResponseException e) {
      return Mono.just(e.getResponseBodyAsString());
    }
    return Mono.just("Error: " + error.getMessage());
  }

  private JSONObject parseJsonResponse(String response) {
    try {
      return new JSONObject(response);
    } catch (Exception e) {
      log.warn("Failed to parse JSON, returning as string: {}", e.getMessage());
      JSONObject obj = new JSONObject();
      obj.put("data", response);
      return obj;
    }
  }

  private JSONObject handleJsonError(Throwable error) {
    log.error("JSON request error: {}", error.getMessage());
    return createErrorResponse("999", error.getMessage());
  }

  private JSONObject createStructuredResponse(String statusCode, String statusText, String responseBody) {
    JSONObject obj = new JSONObject();
    obj.put("StatusCode", statusCode);
    obj.put("StatusText", statusText);
    obj.put("ResponseBody", responseBody);
    return obj;
  }

  private JSONObject createErrorResponse(String statusCode, String errorMessage) {
    JSONObject obj = new JSONObject();
    obj.put("StatusCode", statusCode);
    obj.put("StatusText", "Error");
    obj.put("ResponseBody", errorMessage);
    obj.put("Error", errorMessage);
    return obj;
  }
}
