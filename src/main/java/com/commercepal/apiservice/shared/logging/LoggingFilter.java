package com.commercepal.apiservice.shared.logging;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;

/**
 * HTTP Request/Response Logging Filter
 * <p>
 * Features: - Automatic correlation ID generation and propagation - Request/Response body logging
 * (with size limits) - Performance metrics (request duration) - MDC context for structured logging
 * - Sensitive data masking - Comprehensive error tracking
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class LoggingFilter implements Filter {

  private static final int MAX_PAYLOAD_LENGTH = 10000; // 10KB
  private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
  private static final String REQUEST_ID_HEADER = "X-Request-ID";
  private static final String TRACE_ID_HEADER = "X-Trace-ID";
  private static final String TRACE_ID_ATTRIBUTE = "TRACE_ID";
  private static final String REQUEST_ID_ATTRIBUTE = "REQUEST_ID";

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    // Generate or extract correlation ID
    String correlationId = getOrGenerateCorrelationId(httpRequest);
    String requestId = UUID.randomUUID().toString();

    ContentCachingRequestWrapper wrappedRequest;
    if (httpRequest instanceof ContentCachingRequestWrapper) {
      wrappedRequest = (ContentCachingRequestWrapper) httpRequest;
    } else {
      wrappedRequest = new ContentCachingRequestWrapper(httpRequest, MAX_PAYLOAD_LENGTH);
    }

    // Set MDC context
    MDC.put("correlationId", correlationId);
    MDC.put("requestId", requestId);
    MDC.put("traceId", correlationId);
    MDC.put("spanId", requestId);

    // Expose identifiers to downstream components and clients
    httpRequest.setAttribute(TRACE_ID_ATTRIBUTE, correlationId);
    httpRequest.setAttribute(REQUEST_ID_ATTRIBUTE, requestId);

    // Add correlation ID to response headers
    httpResponse.setHeader(CORRELATION_ID_HEADER, correlationId);
    httpResponse.setHeader(REQUEST_ID_HEADER, requestId);
    httpResponse.setHeader(TRACE_ID_HEADER, correlationId);

    Instant startTime = Instant.now();

    try {
      String method = httpRequest.getMethod();
      logRequest(wrappedRequest, correlationId, requestId);

      chain.doFilter(wrappedRequest, httpResponse);

      logRequestBody(wrappedRequest, method);
      logResponseMetadata(httpRequest, httpResponse, correlationId, requestId, startTime);

    } catch (Exception e) {
      logError(wrappedRequest, e, correlationId, requestId, startTime);
      throw e;
    } finally {
      // Clear MDC
      MDC.clear();
    }
  }

  private String getOrGenerateCorrelationId(HttpServletRequest request) {
    String correlationId = request.getHeader(CORRELATION_ID_HEADER);
    if (correlationId == null || correlationId.isEmpty()) {
      correlationId = request.getHeader(TRACE_ID_HEADER);
    }
    if (correlationId == null || correlationId.isEmpty()) {
      correlationId = UUID.randomUUID().toString();
    }
    return correlationId;
  }

  private void logRequest(ContentCachingRequestWrapper request, String correlationId,
      String requestId) {
    String method = request.getMethod();
    String uri = request.getRequestURI();
    String queryString = request.getQueryString();
    String clientIp = getClientIp(request);

    // Add to MDC for structured logging
    MDC.put("method", method);
    MDC.put("endpoint", uri);
    MDC.put("ipAddress", clientIp);

    log.info("╔════════════════════════════════════════════════════════════════════════════════╗");
    log.info("║ INCOMING REQUEST                                                               ║");
    log.info("╠════════════════════════════════════════════════════════════════════════════════╣");
    log.info("║ Method: {} | URI: {}", method, uri);
    log.info("║ Query: {}", queryString != null ? queryString : "N/A");
    log.info("║ Client IP: {}", clientIp);
    log.info("║ User-Agent: {}", request.getHeader("User-Agent"));
    log.info("║ Correlation ID: {}", correlationId);
    log.info("║ Request ID: {}", requestId);
    log.info("╚════════════════════════════════════════════════════════════════════════════════╝");

    // Request body will be logged after filter chain processes it
  }

  /**
   * Log response metadata without body (to avoid caching issues)
   */
  private void logResponseMetadata(HttpServletRequest request,
      HttpServletResponse response,
      String correlationId,
      String requestId,
      Instant startTime) {

    long duration = Duration.between(startTime, Instant.now()).toMillis();
    int status = response.getStatus();
    String method = request.getMethod();
    String uri = request.getRequestURI();

    // Add to MDC
    MDC.put("statusCode", String.valueOf(status));
    MDC.put("duration", String.valueOf(duration));

    log.info("╔════════════════════════════════════════════════════════════════════════════════╗");
    log.info("║ OUTGOING RESPONSE                                                              ║");
    log.info("╠════════════════════════════════════════════════════════════════════════════════╣");
    log.info("║ Method: {} | URI: {}", method, uri);
    log.info("║ Status: {} | Duration: {} ms", status, duration);
    log.info("║ Correlation ID: {}", correlationId);
    log.info("║ Request ID: {}", requestId);
    log.info("║ Content-Type: {}", response.getContentType());
    log.info("╚════════════════════════════════════════════════════════════════════════════════╝");
  }


  private void logError(ContentCachingRequestWrapper request,
      Exception e,
      String correlationId,
      String requestId,
      Instant startTime) {

    long duration = Duration.between(startTime, Instant.now()).toMillis();
    String method = request.getMethod();
    String uri = request.getRequestURI();

    log.error("╔════════════════════════════════════════════════════════════════════════════════╗");
    log.error("║ REQUEST ERROR                                                                  ║");
    log.error("╠════════════════════════════════════════════════════════════════════════════════╣");
    log.error("║ Method: {} | URI: {}", method, uri);
    log.error("║ Duration: {} ms", duration);
    log.error("║ Correlation ID: {}", correlationId);
    log.error("║ Request ID: {}", requestId);
    log.error("║ Error: {}", e.getMessage());
    log.error("╚════════════════════════════════════════════════════════════════════════════════╝");
    log.error("Exception details:", e);
  }

  private String getClientIp(HttpServletRequest request) {
    String ip = request.getHeader("X-Forwarded-For");
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("X-Real-IP");
    }
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getRemoteAddr();
    }
    return ip;
  }

  private boolean hasBody(String method) {
    return "POST".equalsIgnoreCase(method) ||
        "PUT".equalsIgnoreCase(method) ||
        "PATCH".equalsIgnoreCase(method);
  }

  private void logRequestBody(ContentCachingRequestWrapper request, String method) {
    if (hasBody(method)) {
      byte[] content = request.getContentAsByteArray();
      if (content != null && content.length > 0) {
        int length = Math.min(content.length, MAX_PAYLOAD_LENGTH);
        String body = new String(content, 0, length, StandardCharsets.UTF_8);
        if (content.length > MAX_PAYLOAD_LENGTH) {
          body += "... (truncated)";
        }
        log.debug("Request Body: {}", maskSensitiveData(body));
      }
    }
  }

  /**
   * Mask sensitive data in logs (passwords, tokens, credit cards, etc.)
   */
  private String maskSensitiveData(String data) {
    if (data == null) {
      return null;
    }

    return data
        .replaceAll("(\"password\"\\s*:\\s*\")([^\"]+)(\")", "$1***MASKED***$3")
        .replaceAll("(\"token\"\\s*:\\s*\")([^\"]+)(\")", "$1***MASKED***$3")
        .replaceAll("(\"apiKey\"\\s*:\\s*\")([^\"]+)(\")", "$1***MASKED***$3")
        .replaceAll("(\"secret\"\\s*:\\s*\")([^\"]+)(\")", "$1***MASKED***$3")
        .replaceAll("(\"creditCard\"\\s*:\\s*\")([^\"]+)(\")", "$1***MASKED***$3")
        .replaceAll("(\"cvv\"\\s*:\\s*\")([^\"]+)(\")", "$1***MASKED***$3")
        .replaceAll("(\"ssn\"\\s*:\\s*\")([^\"]+)(\")", "$1***MASKED***$3")
        .replaceAll("(Bearer\\s+)([\\w-]+\\.?[\\w-]+\\.?[\\w-]+)", "$1***MASKED***");
  }
}

