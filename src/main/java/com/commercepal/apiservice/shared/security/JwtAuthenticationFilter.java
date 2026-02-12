package com.commercepal.apiservice.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Enhanced JWT Authentication Filter with comprehensive security features including device
 * tracking, threat detection, and audit logging. Production-ready for international remittance
 * systems.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;
  private final CustomUserDetailsService userDetailsService;
  private final SecurityAuditService securityAuditService;

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    try {
      String token = extractTokenFromRequest(request);

      if (token != null) {
        String deviceFingerprint = extractDeviceFingerprint(request);
        String ipAddress = getClientIpAddress(request);

        // Validate token with security context
        if (jwtTokenProvider.validateToken(token, deviceFingerprint, ipAddress)) {

          // Extract user security context
          JwtTokenProvider.UserSecurityContext securityContext = jwtTokenProvider.getUserSecurityContext(
              token);

          if (securityContext != null) {
            // Extract username from security context, with fallback to email or
            // phonePrimary
            String loginIdentifier = securityContext.getLoginIdentifier();
            if (loginIdentifier == null || loginIdentifier.trim().isEmpty()) {
              log.warn("[JWT-FILTER] Cannot extract loginIdentifier from token for IP: {}",
                  ipAddress);
              handleInvalidToken(request, response);
              return;
            }

            // Store loginIdentifier in a final variable for use in anonymous class
            final String finalLoginIdentifier = loginIdentifier.trim();

            // Load user details
            UserDetails userDetails = userDetailsService.loadUserByUsername(finalLoginIdentifier);

            // Perform additional security checks
            if (performSecurityChecks(securityContext, request, ipAddress, deviceFingerprint)) {

              // Create a UserDetails wrapper that ensures getUsername() returns the
              // loginIdentifier
              // This is necessary because User.getUsername() might be null if user logged in
              // with email/phone
              UserDetails principalWithLoginIdentifier = new UserDetails() {
                @Override
                public String getUsername() {
                  return finalLoginIdentifier; // Always return the login identifier used for authentication
                }

                @Override
                public String getPassword() {
                  return userDetails.getPassword();
                }

                @Override
                public Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
                  return userDetails.getAuthorities();
                }

                @Override
                public boolean isAccountNonExpired() {
                  return userDetails.isAccountNonExpired();
                }

                @Override
                public boolean isAccountNonLocked() {
                  return userDetails.isAccountNonLocked();
                }

                @Override
                public boolean isCredentialsNonExpired() {
                  return userDetails.isCredentialsNonExpired();
                }

                @Override
                public boolean isEnabled() {
                  return userDetails.isEnabled();
                }
              };

              // Create authentication with wrapper that ensures correct username
              UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                  principalWithLoginIdentifier, null, userDetails.getAuthorities());

              // Set additional security details
              authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

              // Set security context
              SecurityContextHolder.getContext().setAuthentication(authentication);

              // Update last activity
              updateUserActivity(securityContext, ipAddress, deviceFingerprint);

              log.debug("[JWT-FILTER] Successfully authenticated user: {} from IP: {}",
                  securityContext.getLoginIdentifier(), ipAddress);
            } else {
              log.warn("[JWT-FILTER] Security checks failed for user: {} from IP: {}",
                  securityContext.getLoginIdentifier(), ipAddress);
              handleSecurityCheckFailure(request, response, securityContext.getLoginIdentifier(),
                  ipAddress);
              return;
            }
          }
        } else {
          log.debug("[JWT-FILTER] Invalid JWT token from IP: {}", ipAddress);
          handleInvalidToken(request, response);
          handleInvalidToken(request, response);
          return;
        }
      }
    } catch (UsernameNotFoundException e) {
      // Handle UsernameNotFoundException with security-aware response
      // This matches the handling in ApplicationExceptionHandler
      String clientIp = getClientIpAddress(request);
      String userAgent = request.getHeader("User-Agent");

      log.warn(
          "[SECURITY] UsernameNotFoundException in JWT filter - Method: {} | URI: {} | IP: {} | User-Agent: {} | Message: {}",
          request.getMethod(), request.getRequestURI(), clientIp, userAgent, e.getMessage());

      // Return UNAUTHORIZED with generic message to prevent user enumeration
      handleUsernameNotFound(request, response);
      return;
    } catch (Exception e) {
      log.error("[JWT-FILTER] JWT authentication error: {}", e.getMessage(), e);
      handleAuthenticationError(request, response, e);
      return;
    }

    filterChain.doFilter(request, response);
  }

  /**
   * Extract JWT token from request
   */
  private String extractTokenFromRequest(HttpServletRequest request) {
    // Check Authorization header first
    String bearerToken = request.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }

    // Check for token in cookie (for web applications)
    if (request.getCookies() != null) {
      for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
        if ("access_token".equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }

    // Check query parameter (for websocket connections)
    String tokenParam = request.getParameter("token");
    if (tokenParam != null && !tokenParam.isEmpty()) {
      return tokenParam;
    }

    return null;
  }

  /**
   * Extract device fingerprint from request headers
   */
  private String extractDeviceFingerprint(HttpServletRequest request) {
    String fingerprint = request.getHeader("X-Device-Fingerprint");
    if (fingerprint != null && !fingerprint.isEmpty()) {
      return fingerprint;
    }

    // Generate basic fingerprint from available headers
    return generateBasicFingerprint(request);
  }

  /**
   * Generate basic device fingerprint from request headers
   */
  private String generateBasicFingerprint(HttpServletRequest request) {
    StringBuilder fingerprint = new StringBuilder();

    String userAgent = request.getHeader("User-Agent");
    if (userAgent != null) {
      fingerprint.append(userAgent.hashCode());
    }

    String acceptLanguage = request.getHeader("Accept-Language");
    if (acceptLanguage != null) {
      fingerprint.append("-").append(acceptLanguage.hashCode());
    }

    String acceptEncoding = request.getHeader("Accept-Encoding");
    if (acceptEncoding != null) {
      fingerprint.append("-").append(acceptEncoding.hashCode());
    }

    return !fingerprint.isEmpty() ? fingerprint.toString() : "unknown";
  }

  /**
   * Get client IP address with proxy support
   */
  private String getClientIpAddress(HttpServletRequest request) {
    String[] headers = {"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP",
        "HTTP_X_FORWARDED_FOR", "HTTP_X_FORWARDED", "HTTP_X_CLUSTER_CLIENT_IP", "HTTP_CLIENT_IP",
        "HTTP_FORWARDED_FOR", "HTTP_FORWARDED", "HTTP_VIA", "REMOTE_ADDR"};

    for (String header : headers) {
      String ip = request.getHeader(header);
      if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
        // Handle multiple IPs in X-Forwarded-For
        if (ip.contains(",")) {
          ip = ip.split(",")[0].trim();
        }
        return ip;
      }
    }

    return request.getRemoteAddr();
  }

  /**
   * Perform additional security checks
   */
  private boolean performSecurityChecks(JwtTokenProvider.UserSecurityContext securityContext,
      HttpServletRequest request, String ipAddress, String deviceFingerprint) {

    // Check if user account is still active
    if (!userDetailsService.isUserActive(securityContext.getLoginIdentifier())) {
      log.warn("[JWT-FILTER] Inactive user attempted access: {}",
          securityContext.getLoginIdentifier());
      securityAuditService.auditSuspiciousActivity(securityContext.getLoginIdentifier(),
          "INACTIVE_USER_ACCESS", deviceFingerprint, ipAddress);
      return false;
    }

    // Check risk score
    if (securityContext.getRiskScore() != null && securityContext.getRiskScore() > 80) {
      log.warn("[JWT-FILTER] High risk score for user: {} score: {}",
          securityContext.getLoginIdentifier(), securityContext.getRiskScore());

      // Allow access but require additional verification for sensitive operations
      request.setAttribute("HIGH_RISK_USER", true);
    }

    // Check MFA requirement for high-value operations
    String requestURI = request.getRequestURI();
    if (isHighValueOperation(requestURI) && !Boolean.TRUE.equals(
        securityContext.getMfaVerified())) {
      log.warn("[JWT-FILTER] MFA required for high-value operation: {} user: {}", requestURI,
          securityContext.getLoginIdentifier());
      request.setAttribute("MFA_REQUIRED", true);
    }

    // Check session validity
    // if (!isSessionValid(securityContext)) {
    // log.warn("[JWT-FILTER] Invalid session for user: {}",
    // securityContext.getLoginIdentifier());
    // return false;
    // }

    return true;
  }

  /**
   * Check if the operation requires high security
   */
  private boolean isHighValueOperation(String requestURI) {
    return requestURI.contains("/transactions/") && (requestURI.contains("/approve")
        || requestURI.contains("/transfer") || requestURI.contains("/process"));
  }

  /**
   * Check if session is still valid
   */
  private boolean isSessionValid(JwtTokenProvider.UserSecurityContext securityContext) {
    // Check session expiration
    return !securityContext.getExpiresAt().isBefore(LocalDateTime.now());

    // Additional session validation logic can be added here
  }

  /**
   * Update user activity tracking
   */
  private void updateUserActivity(JwtTokenProvider.UserSecurityContext securityContext,
      String ipAddress, String deviceFingerprint) {
    try {
      userDetailsService.updateLastActivity(securityContext.getLoginIdentifier());
      log.debug("[JWT-FILTER] Updated activity for user: {} from IP: {}",
          securityContext.getLoginIdentifier(), ipAddress);
    } catch (Exception e) {
      log.error("[JWT-FILTER] Failed to update user activity: {}", e.getMessage());
    }
  }

  /**
   * Handle security check failure
   */
  private void handleSecurityCheckFailure(HttpServletRequest request, HttpServletResponse response,
      String username, String ipAddress) throws IOException {
    writeErrorResponse(request, response, HttpServletResponse.SC_FORBIDDEN,
        "SECURITY_CHECK_FAILED",
        "Access denied due to security policy",
        null);
  }

  /**
   * Handle invalid token
   */
  private void handleInvalidToken(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    writeErrorResponse(request, response, HttpServletResponse.SC_UNAUTHORIZED, "INVALID_TOKEN",
        "Authentication token is invalid or expired", null);
  }

  /**
   * Handle UsernameNotFoundException with security-aware response. This occurs when a valid JWT
   * token references a user that no longer exists in the database. This is a "stale token" scenario
   * - the token is valid but the user was deleted/deactivated.
   */
  private void handleUsernameNotFound(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    // Message indicates the token is invalid (user doesn't exist), not that
    // credentials are wrong
    String message = "Your token is no longer valid. Please log in again.";

    writeErrorResponse(request, response, HttpServletResponse.SC_UNAUTHORIZED, "INVALID_TOKEN",
        message, null);
  }

  /**
   * Handle authentication error
   */
  private void handleAuthenticationError(HttpServletRequest request,
      HttpServletResponse response, Exception e) throws IOException {
    log.error("[JWT-FILTER] Authentication error: {}", e.getMessage());
    writeErrorResponse(request, response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
        "AUTHENTICATION_ERROR", "Internal authentication error", null);
  }

  private void writeErrorResponse(HttpServletRequest request, HttpServletResponse response,
      int status, String errorCode, String message, String validationErrorsJson)
      throws IOException {
    if (!response.isCommitted()) {
      response.resetBuffer();
    }
    response.setStatus(status);
    response.setContentType("application/json");

    String path = request != null && request.getRequestURI() != null ? request.getRequestURI() : "";
    String method = request != null && request.getMethod() != null ? request.getMethod() : "";
    String traceId = resolveTraceId(request);
    String safeErrorCode = escapeJson(errorCode);
    String safeMessage = escapeJson(message);
    String safePath = escapeJson(path);
    String safeMethod = escapeJson(method);
    String safeTraceId = escapeJson(traceId);

    StringBuilder payload = new StringBuilder();
    payload.append("{");
    payload.append("\"status\":").append(status).append(",");
    payload.append("\"errorCode\":\"").append(safeErrorCode).append("\",");
    payload.append("\"message\":\"").append(safeMessage).append("\",");
    payload.append("\"timestamp\":\"").append(Instant.now()).append("\",");
    payload.append("\"path\":\"").append(safePath).append("\",");
    payload.append("\"method\":\"").append(safeMethod).append("\",");
    payload.append("\"validationErrors\":");
    if (validationErrorsJson == null || validationErrorsJson.trim().isEmpty()) {
      payload.append("null");
    } else {
      payload.append(validationErrorsJson.trim());
    }
    payload.append(",");
    payload.append("\"traceId\":\"").append(safeTraceId).append("\"");
    payload.append("}");

    response.getWriter().write(payload.toString());
  }

  private String resolveTraceId(HttpServletRequest request) {
    if (request == null) {
      return UUID.randomUUID().toString();
    }

    Object traceAttribute = request.getAttribute("traceId");
    if (traceAttribute == null) {
      traceAttribute = request.getAttribute("TRACE_ID");
    }
    if (traceAttribute instanceof String && !((String) traceAttribute).isBlank()) {
      return (String) traceAttribute;
    }

    String headerTraceId = request.getHeader("X-Trace-Id");
    if (headerTraceId == null || headerTraceId.isBlank()) {
      headerTraceId = request.getHeader("X-Request-Id");
    }
    if (headerTraceId != null && !headerTraceId.isBlank()) {
      return headerTraceId;
    }

    return UUID.randomUUID().toString();
  }

  private String escapeJson(String value) {
    if (value == null) {
      return "";
    }

    StringBuilder escaped = new StringBuilder();
    for (char c : value.toCharArray()) {
      switch (c) {
        case '"':
          escaped.append("\\\"");
          break;
        case '\\':
          escaped.append("\\\\");
          break;
        case '\b':
          escaped.append("\\b");
          break;
        case '\f':
          escaped.append("\\f");
          break;
        case '\n':
          escaped.append("\\n");
          break;
        case '\r':
          escaped.append("\\r");
          break;
        case '\t':
          escaped.append("\\t");
          break;
        default:
          if (c < 0x20 || c > 0x7E) {
            escaped.append(String.format("\\u%04x", (int) c));
          } else {
            escaped.append(c);
          }
      }
    }
    return escaped.toString();
  }

  /**
   * Skip authentication for certain paths
   */
  @Override
  protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
    String path = request.getRequestURI();

    // Skip authentication for public endpoints
    // Verify public auth endpoints explicitly
    if (path.startsWith("/api/v1/auth/")) {
      return path.equals("/api/v1/auth/login") ||
          path.equals("/api/v1/auth/refresh") ||
          path.equals("/api/v1/auth/oauth2/login");
    }

    // Skip authentication for likely public endpoints
    return path.startsWith("/api/v1/public/")
        || path.startsWith("/api/v1/health/")
        || path.startsWith("/api/v1/categories/")
        || path.startsWith("/api/v1/subcategories/")
        || path.startsWith("/swagger-ui/")
        || path.startsWith("/v3/api-docs/")
        || path.equals("/favicon.ico");
  }
}
