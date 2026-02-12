package com.commercepal.apiservice.shared.security.check;//package com.fastpay.agent.shared.security;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.time.temporal.ChronoUnit;
//import java.util.*;
//import java.util.concurrent.TimeUnit;
//
/// **
// * Advanced session management service with concurrent session control,
// * session tracking, and security monitoring.
// */
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class SessionManagementService {
//
//    private final RedisTemplate<String, Object> redisTemplate;
//    private final SecurityAuditService securityAuditService;
//
//    // Session configuration
//    private static final int MAX_CONCURRENT_SESSIONS_PER_USER = 3;
//    private static final int SESSION_TIMEOUT_MINUTES = 30;
//    private static final int EXTENDED_SESSION_TIMEOUT_HOURS = 8;
//    private static final int MAX_INACTIVE_MINUTES = 15;
//
//    /**
//     * Create a new user session
//     */
//    public UserSession createSession(Long userId, String username, String deviceFingerprint,
//            String ipAddress, String userAgent, boolean rememberMe) {
//        try {
//            // Check concurrent session limits
//            List<UserSession> activeSessions = getActiveSessions(userId);
//            if (activeSessions.size() >= MAX_CONCURRENT_SESSIONS_PER_USER) {
//                // Terminate oldest session
//                terminateOldestSession(userId, activeSessions);
//            }
//
//            // Generate session ID
//            String sessionId = generateSessionId();
//
//            // Create session object
//            UserSession session = UserSession.builder()
//                    .sessionId(sessionId)
//                    .userId(userId)
//                    .username(username)
//                    .deviceFingerprint(deviceFingerprint)
//                    .ipAddress(ipAddress)
//                    .userAgent(userAgent)
//                    .createdAt(LocalDateTime.now())
//                    .lastAccessedAt(LocalDateTime.now())
//                    .expiresAt(LocalDateTime.now().plus(
//     rememberMe ? EXTENDED_SESSION_TIMEOUT_HOURS : SESSION_TIMEOUT_MINUTES,
//     rememberMe ? ChronoUnit.HOURS : ChronoUnit.MINUTES))
//                    .isActive(true)
//                    .rememberMe(rememberMe)
//                    .build();
//
//            // Store session in Redis
//            String sessionKey = getSessionKey(sessionId);
//            String userSessionsKey = getUserSessionsKey(userId);
//
//            long timeoutMinutes = rememberMe ? (EXTENDED_SESSION_TIMEOUT_HOURS * 60) : SESSION_TIMEOUT_MINUTES;
//
//            redisTemplate.opsForValue().set(sessionKey, session, timeoutMinutes, TimeUnit.MINUTES);
//            redisTemplate.opsForSet().add(userSessionsKey, sessionId);
//            redisTemplate.expire(userSessionsKey, timeoutMinutes, TimeUnit.MINUTES);
//
//            // Audit session creation
//            securityAuditService.auditSessionCreated(username, sessionId, deviceFingerprint, ipAddress);
//
//            log.info("Created session for user: {} sessionId: {} from IP: {}", username, sessionId, ipAddress);
//            return session;
//
//        } catch (Exception e) {
//            log.error("Failed to create session for user: {}", userId, e);
//            throw new SessionManagementException("Failed to create session", e);
//        }
//    }
//
//    /**
//     * Get session by session ID
//     */
//    public UserSession getSession(String sessionId) {
//        try {
//            String sessionKey = getSessionKey(sessionId);
//            UserSession session = (UserSession) redisTemplate.opsForValue().get(sessionKey);
//
//            if (session != null && isSessionValid(session)) {
//                return session;
//            }
//
//            return null;
//        } catch (Exception e) {
//            log.error("Failed to get session: {}", sessionId, e);
//            return null;
//        }
//    }
//
//    /**
//     * Update session activity
//     */
//    public boolean updateSessionActivity(String sessionId, String ipAddress) {
//        try {
//            UserSession session = getSession(sessionId);
//            if (session == null) {
//                return false;
//            }
//
//            // Check for IP change
//            if (!session.getIpAddress().equals(ipAddress)) {
//                log.warn("IP address change detected for session: {} from {} to {}",
// sessionId, session.getIpAddress(), ipAddress);
//
//                // Audit IP change
//                securityAuditService.auditSessionIpChange(session.getUsername(), sessionId,
// session.getIpAddress(), ipAddress);
//
//                // Update IP in session
//                session.setIpAddress(ipAddress);
//            }
//
//            // Update last accessed time
//            session.setLastAccessedAt(LocalDateTime.now());
//
//            // Extend expiration if remember me is enabled
//            if (session.getRememberMe()) {
//                session.setExpiresAt(LocalDateTime.now().plusHours(EXTENDED_SESSION_TIMEOUT_HOURS));
//            } else {
//                session.setExpiresAt(LocalDateTime.now().plusMinutes(SESSION_TIMEOUT_MINUTES));
//            }
//
//            // Save updated session
//            String sessionKey = getSessionKey(sessionId);
//            long timeoutMinutes = session.getRememberMe() ? (EXTENDED_SESSION_TIMEOUT_HOURS * 60) : SESSION_TIMEOUT_MINUTES;
//            redisTemplate.opsForValue().set(sessionKey, session, timeoutMinutes, TimeUnit.MINUTES);
//
//            return true;
//
//        } catch (Exception e) {
//            log.error("Failed to update session activity: {}", sessionId, e);
//            return false;
//        }
//    }
//
//    /**
//     * Terminate session
//     */
//    public boolean terminateSession(String sessionId, String reason) {
//        try {
//            UserSession session = getSession(sessionId);
//            if (session == null) {
//                return false;
//            }
//
//            // Mark session as inactive
//            session.setIsActive(false);
//            session.setTerminatedAt(LocalDateTime.now());
//            session.setTerminationReason(reason);
//
//            // Remove from Redis
//            String sessionKey = getSessionKey(sessionId);
//            String userSessionsKey = getUserSessionsKey(session.getUserId());
//
//            redisTemplate.delete(sessionKey);
//            redisTemplate.opsForSet().remove(userSessionsKey, sessionId);
//
//            // Audit session termination
//            securityAuditService.auditSessionTerminated(session.getUsername(), sessionId, reason);
//
//            log.info("Terminated session: {} for user: {} reason: {}",
//                    sessionId, session.getUsername(), reason);
//            return true;
//
//        } catch (Exception e) {
//            log.error("Failed to terminate session: {}", sessionId, e);
//            return false;
//        }
//    }
//
//    /**
//     * Terminate all sessions for a user
//     */
//    public int terminateAllUserSessions(Long userId, String reason) {
//        try {
//            List<UserSession> activeSessions = getActiveSessions(userId);
//            int terminatedCount = 0;
//
//            for (UserSession session : activeSessions) {
//                if (terminateSession(session.getSessionId(), reason)) {
//                    terminatedCount++;
//                }
//            }
//
//            log.info("Terminated {} sessions for user: {} reason: {}", terminatedCount, userId, reason);
//            return terminatedCount;
//
//        } catch (Exception e) {
//            log.error("Failed to terminate all sessions for user: {}", userId, e);
//            return 0;
//        }
//    }
//
//    /**
//     * Terminate all sessions except current
//     */
//    public int terminateOtherSessions(Long userId, String currentSessionId, String reason) {
//        try {
//            List<UserSession> activeSessions = getActiveSessions(userId);
//            int terminatedCount = 0;
//
//            for (UserSession session : activeSessions) {
//                if (!session.getSessionId().equals(currentSessionId)) {
//                    if (terminateSession(session.getSessionId(), reason)) {
// terminatedCount++;
//                    }
//                }
//            }
//
//            log.info("Terminated {} other sessions for user: {} reason: {}", terminatedCount, userId, reason);
//            return terminatedCount;
//
//        } catch (Exception e) {
//            log.error("Failed to terminate other sessions for user: {}", userId, e);
//            return 0;
//        }
//    }
//
//    /**
//     * Get all active sessions for a user
//     */
//    public List<UserSession> getActiveSessions(Long userId) {
//        try {
//            String userSessionsKey = getUserSessionsKey(userId);
//            Set<Object> sessionIds = redisTemplate.opsForSet().members(userSessionsKey);
//
//            List<UserSession> activeSessions = new ArrayList<>();
//
//            if (sessionIds != null) {
//                for (Object sessionIdObj : sessionIds) {
//                    String sessionId = (String) sessionIdObj;
//                    UserSession session = getSession(sessionId);
//
//                    if (session != null && isSessionValid(session)) {
// activeSessions.add(session);
//                    } else {
// // Clean up invalid session reference
// redisTemplate.opsForSet().remove(userSessionsKey, sessionId);
//                    }
//                }
//            }
//
//            return activeSessions;
//
//        } catch (Exception e) {
//            log.error("Failed to get active sessions for user: {}", userId, e);
//            return new ArrayList<>();
//        }
//    }
//
//    /**
//     * Get session statistics for a user
//     */
//    public SessionStatistics getSessionStatistics(Long userId) {
//        try {
//            List<UserSession> activeSessions = getActiveSessions(userId);
//
//            return SessionStatistics.builder()
//                    .userId(userId)
//                    .totalActiveSessions(activeSessions.size())
//                    .maxAllowedSessions(MAX_CONCURRENT_SESSIONS_PER_USER)
//                    .oldestSessionCreated(activeSessions.stream()
//     .map(UserSession::getCreatedAt)
//     .min(LocalDateTime::compareTo)
//     .orElse(null))
//                    .newestSessionCreated(activeSessions.stream()
//     .map(UserSession::getCreatedAt)
//     .max(LocalDateTime::compareTo)
//     .orElse(null))
//                    .uniqueIpAddresses((int) activeSessions.stream()
//     .map(UserSession::getIpAddress)
//     .distinct()
//     .count())
//                    .uniqueDevices((int) activeSessions.stream()
//     .map(UserSession::getDeviceFingerprint)
//     .distinct()
//     .count())
//                    .build();
//
//        } catch (Exception e) {
//            log.error("Failed to get session statistics for user: {}", userId, e);
//            return SessionStatistics.builder().userId(userId).build();
//        }
//    }
//
//    /**
//     * Clean up expired sessions
//     */
//    public int cleanupExpiredSessions() {
//        try {
//            // This would typically be called by a scheduled task
//            // For now, Redis TTL handles most of the cleanup
//            // This method can be used for additional cleanup logic
//
//            log.debug("Session cleanup completed");
//            return 0;
//
//        } catch (Exception e) {
//            log.error("Failed to cleanup expired sessions", e);
//            return 0;
//        }
//    }
//
//    /**
//     * Check if session is valid
//     */
//    private boolean isSessionValid(UserSession session) {
//        if (session == null || !session.getIsActive()) {
//            return false;
//        }
//
//        LocalDateTime now = LocalDateTime.now();
//
//        // Check expiration
//        if (session.getExpiresAt().isBefore(now)) {
//            log.debug("Session expired: {}", session.getSessionId());
//            return false;
//        }
//
//        // Check inactivity timeout
//        if (session.getLastAccessedAt().plusMinutes(MAX_INACTIVE_MINUTES).isBefore(now)) {
//            log.debug("Session inactive timeout: {}", session.getSessionId());
//            return false;
//        }
//
//        return true;
//    }
//
//    /**
//     * Terminate oldest session when limit is reached
//     */
//    private void terminateOldestSession(Long userId, List<UserSession> activeSessions) {
//        UserSession oldestSession = activeSessions.stream()
//                .min(Comparator.comparing(UserSession::getCreatedAt))
//                .orElse(null);
//
//        if (oldestSession != null) {
//            terminateSession(oldestSession.getSessionId(), "CONCURRENT_SESSION_LIMIT_EXCEEDED");
//        }
//    }
//
//    /**
//     * Generate unique session ID
//     */
//    private String generateSessionId() {
//        return "sess_" + UUID.randomUUID().toString().replace("-", "");
//    }
//
//    /**
//     * Get Redis key for session
//     */
//    private String getSessionKey(String sessionId) {
//        return "session:" + sessionId;
//    }
//
//    /**
//     * Get Redis key for user sessions
//     */
//    private String getUserSessionsKey(Long userId) {
//        return "user_sessions:" + userId;
//    }
//
//    /**
//     * User session data structure
//     */
//    @lombok.Data
//    @lombok.Builder
//    public static class UserSession {
//        private String sessionId;
//        private Long userId;
//        private String username;
//        private String deviceFingerprint;
//        private String ipAddress;
//        private String userAgent;
//        private LocalDateTime createdAt;
//        private LocalDateTime lastAccessedAt;
//        private LocalDateTime expiresAt;
//        private LocalDateTime terminatedAt;
//        private Boolean isActive;
//        private Boolean rememberMe;
//        private String terminationReason;
//
//        // Additional metadata
//        private Map<String, Object> attributes = new HashMap<>();
//    }
//
//    /**
//     * Session statistics
//     */
//    @lombok.Data
//    @lombok.Builder
//    public static class SessionStatistics {
//        private Long userId;
//        private Integer totalActiveSessions;
//        private Integer maxAllowedSessions;
//        private LocalDateTime oldestSessionCreated;
//        private LocalDateTime newestSessionCreated;
//        private Integer uniqueIpAddresses;
//        private Integer uniqueDevices;
//    }
//
//    /**
//     * Session management exception
//     */
//    public static class SessionManagementException extends RuntimeException {
//        public SessionManagementException(String message) {
//            super(message);
//        }
//
//        public SessionManagementException(String message, Throwable cause) {
//            super(message, cause);
//        }
//    }
//}
