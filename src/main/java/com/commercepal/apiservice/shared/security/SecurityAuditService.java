package com.commercepal.apiservice.shared.security;

import com.commercepal.apiservice.shared.cache.InMemoryCacheService;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Professional security audit service for comprehensive security event logging and monitoring in
 * the international remittance system. Production-ready with Google-level security standards.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityAuditService {

  private static final String AUDIT_LOG_PREFIX = "audit:security:";
  private static final String SUSPICIOUS_ACTIVITY_PREFIX = "suspicious:";
  private static final String FAILED_LOGIN_PREFIX = "failed_login:";
  private final InMemoryCacheService inMemoryCacheService;

  /**
   * Audit token generation events
   */
  public void auditTokenGeneration(Long userId, String sessionId, String deviceFingerprint,
      String ipAddress) {
    SecurityAuditEvent event = SecurityAuditEvent.builder()
        .eventId(UUID.randomUUID().toString())
        .eventType(SecurityEventType.TOKEN_GENERATED)
        .userId(userId)
        .sessionId(sessionId)
        .deviceFingerprint(deviceFingerprint)
        .ipAddress(ipAddress)
        .timestamp(LocalDateTime.now())
        .severity(SecuritySeverity.INFO)
        .description("JWT token pair generated successfully")
        .additionalData(Map.of("action", "token_generation", "success", true))
        .build();

    logSecurityEvent(event);
  }

  /**
   * Audit suspicious activities
   */
  public void auditSuspiciousActivity(String username, String activityType,
      String deviceFingerprint, String ipAddress) {
    SecurityAuditEvent event = SecurityAuditEvent.builder()
        .eventId(UUID.randomUUID().toString())
        .eventType(SecurityEventType.SUSPICIOUS_ACTIVITY)
        .username(username)
        .deviceFingerprint(deviceFingerprint)
        .ipAddress(ipAddress)
        .timestamp(LocalDateTime.now())
        .severity(SecuritySeverity.WARNING)
        .description("Suspicious activity detected: " + activityType)
        .additionalData(Map.of("activityType", activityType, "riskLevel", "HIGH"))
        .build();

    logSecurityEvent(event);

    // Store in suspicious activity tracking
    String suspiciousKey = SUSPICIOUS_ACTIVITY_PREFIX + username + ":" + activityType;
    inMemoryCacheService.increment(suspiciousKey);
    inMemoryCacheService.expire(suspiciousKey, Duration.ofHours(24));
  }

  /**
   * Audit token blacklisting
   */
  public void auditTokenBlacklist(String username, String tokenId, String reason) {
    SecurityAuditEvent event = SecurityAuditEvent.builder()
        .eventId(UUID.randomUUID().toString())
        .eventType(SecurityEventType.TOKEN_BLACKLISTED)
        .username(username)
        .timestamp(LocalDateTime.now())
        .severity(SecuritySeverity.WARNING)
        .description("Token blacklisted: " + reason)
        .additionalData(Map.of("tokenId", tokenId, "reason", reason))
        .build();

    logSecurityEvent(event);
  }

  /**
   * Audit session invalidation
   */
  public void auditSessionInvalidation(Long userId, String sessionId, String reason) {
    SecurityAuditEvent event = SecurityAuditEvent.builder()
        .eventId(UUID.randomUUID().toString())
        .eventType(SecurityEventType.SESSION_INVALIDATED)
        .userId(userId)
        .sessionId(sessionId)
        .timestamp(LocalDateTime.now())
        .severity(SecuritySeverity.INFO)
        .description("Session invalidated: " + reason)
        .additionalData(Map.of("reason", reason))
        .build();

    logSecurityEvent(event);
  }

  /**
   * Audit failed login
   */
  public void auditFailedLogin(String username, String reason, String deviceFingerprint,
      String ipAddress) {
    SecurityAuditEvent event = SecurityAuditEvent.builder()
        .eventId(UUID.randomUUID().toString())
        .eventType(SecurityEventType.LOGIN_FAILED)
        .username(username)
        .ipAddress(ipAddress)
        .deviceFingerprint(deviceFingerprint)
        .timestamp(LocalDateTime.now())
        .severity(SecuritySeverity.WARNING)
        .description("Failed login attempt: " + reason)
        .additionalData(Map.of("reason", reason != null ? reason : "N/A", "deviceFingerprint",
            deviceFingerprint != null ? deviceFingerprint : ""))
        .build();

    logSecurityEvent(event);

    // Track failed login attempts
    String failedLoginKey = FAILED_LOGIN_PREFIX + username;
    inMemoryCacheService.increment(failedLoginKey);
    inMemoryCacheService.expire(failedLoginKey, Duration.ofMinutes(15));
  }

  /**
   * Audit successful login
   */
  public void auditSuccessfulLogin(String username, String deviceFingerprint, String ipAddress) {
    SecurityAuditEvent event = SecurityAuditEvent.builder()
        .eventId(UUID.randomUUID().toString())
        .eventType(SecurityEventType.LOGIN_SUCCESS)
        .username(username)
        .ipAddress(ipAddress)
        .deviceFingerprint(deviceFingerprint)
        .timestamp(LocalDateTime.now())
        .severity(SecuritySeverity.INFO)
        .description("Successful login")
        .additionalData(
            Map.of("deviceFingerprint", deviceFingerprint != null ? deviceFingerprint : ""))
        .build();

    logSecurityEvent(event);
  }

  /**
   * Audit account locked
   */
  public void auditAccountLocked(String username, String reason, String deviceFingerprint,
      String ipAddress) {
    SecurityAuditEvent event = SecurityAuditEvent.builder()
        .eventId(UUID.randomUUID().toString())
        .eventType(SecurityEventType.ACCOUNT_LOCKED)
        .username(username)
        .ipAddress(ipAddress)
        .timestamp(LocalDateTime.now())
        .severity(SecuritySeverity.CRITICAL)
        .description("Account locked: " + reason)
        .additionalData(Map.of("reason", reason, "deviceFingerprint",
            deviceFingerprint != null ? deviceFingerprint : ""))
        .build();

    logSecurityEvent(event);
  }

  /**
   * Audit session created
   */
  public void auditSessionCreated(String username, String sessionId, String deviceFingerprint,
      String ipAddress) {
    SecurityAuditEvent event = SecurityAuditEvent.builder()
        .eventId(UUID.randomUUID().toString())
        .eventType(SecurityEventType.SESSION_CREATED)
        .username(username)
        .sessionId(sessionId)
        .ipAddress(ipAddress)
        .deviceFingerprint(deviceFingerprint)
        .timestamp(LocalDateTime.now())
        .severity(SecuritySeverity.INFO)
        .description("Session created")
        .additionalData(Map.of("sessionId", sessionId, "deviceFingerprint",
            deviceFingerprint != null ? deviceFingerprint : ""))
        .build();

    logSecurityEvent(event);
  }

  /**
   * Audit privilege escalation attempts
   */
  public void auditPrivilegeEscalation(Long userId, String username, String attemptedAction,
      String requiredRole, String ipAddress) {
    SecurityAuditEvent event = SecurityAuditEvent.builder()
        .eventId(UUID.randomUUID().toString())
        .eventType(SecurityEventType.PRIVILEGE_ESCALATION_ATTEMPT)
        .userId(userId)
        .username(username)
        .ipAddress(ipAddress)
        .timestamp(LocalDateTime.now())
        .severity(SecuritySeverity.CRITICAL)
        .description("Unauthorized privilege escalation attempt")
        .additionalData(Map.of("attemptedAction", attemptedAction, "requiredRole", requiredRole))
        .build();

    logSecurityEvent(event);
  }

  /**
   * Get failed login count for user
   */
  public int getFailedLoginCount(String username) {
    String failedLoginKey = FAILED_LOGIN_PREFIX + username;
    Long count = inMemoryCacheService.getCounter(failedLoginKey);
    return count != null ? count.intValue() : 0;
  }

  /**
   * Get suspicious activity count for user
   */
  public int getSuspiciousActivityCount(String username, String activityType) {
    String suspiciousKey = SUSPICIOUS_ACTIVITY_PREFIX + username + ":" + activityType;
    Long count = inMemoryCacheService.getCounter(suspiciousKey);
    return count != null ? count.intValue() : 0;
  }

  /**
   * Clear failed login attempts (after successful login)
   */
  public void clearFailedLoginAttempts(String username) {
    String failedLoginKey = FAILED_LOGIN_PREFIX + username;
    inMemoryCacheService.delete(failedLoginKey);
  }

  /**
   * Log security event to multiple destinations
   */
  private void logSecurityEvent(SecurityAuditEvent event) {
    try {
      // Log to application logs
      String logMessage = String.format(
          "SECURITY_AUDIT: [%s] %s - User: %s, IP: %s, Event: %s", event.getSeverity(),
          event.getEventType(),
          event.getUsername() != null ? event.getUsername() : "N/A",
          event.getIpAddress() != null ? event.getIpAddress() : "N/A", event.getDescription());

      switch (event.getSeverity()) {
        case CRITICAL -> log.error(logMessage);
        case WARNING -> log.warn(logMessage);
        case INFO -> log.info(logMessage);
        case DEBUG -> log.debug(logMessage);
      }

      // Store in cache for real-time monitoring
      String auditKey = AUDIT_LOG_PREFIX + event.getEventType() + ":" + event.getEventId();
      inMemoryCacheService.set(auditKey, event, Duration.ofDays(30));

      // Store in time-series for analytics
      String timeSeriesKey =
          "audit:timeseries:" + event.getEventType() + ":" + event.getTimestamp().toLocalDate();
      inMemoryCacheService.rightPush(timeSeriesKey, event);
      inMemoryCacheService.expire(timeSeriesKey, Duration.ofDays(90));

    } catch (Exception e) {
      log.error("[SECURITY-AUDIT] Failed to log security audit event: {}", e.getMessage(), e);
    }
  }

  // ============================================
  // INNER CLASSES
  // ============================================

  public enum SecurityEventType {
    // Authentication events
    LOGIN_SUCCESS, LOGIN_FAILED, LOGOUT, TOKEN_GENERATED, TOKEN_BLACKLISTED, TOKEN_REFRESHED,
    // Session events
    SESSION_CREATED, SESSION_INVALIDATED, SESSION_TERMINATED, SESSION_EXPIRED,
    // Security events
    SUSPICIOUS_ACTIVITY, PRIVILEGE_ESCALATION_ATTEMPT, ACCESS_DENIED, ACCOUNT_LOCKED,
    ACCOUNT_UNLOCKED, PASSWORD_CHANGED, PASSWORD_RESET,
    // MFA events
    MFA_SUCCESS, MFA_FAILED, MFA_ENABLED, MFA_DISABLED,
    // Data events
    DATA_ACCESS, DATA_MODIFIED, DATA_DELETED, CONFIGURATION_CHANGED
  }

  public enum SecuritySeverity {
    DEBUG, INFO, WARNING, CRITICAL
  }

  @Data
  @Builder
  public static class SecurityAuditEvent {

    private String eventId;
    private SecurityEventType eventType;
    private Long userId;
    private String username;
    private String sessionId;
    private String deviceFingerprint;
    private String ipAddress;
    private LocalDateTime timestamp;
    private SecuritySeverity severity;
    private String description;
    private Map<String, Object> additionalData;
  }
}
