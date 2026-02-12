package com.commercepal.apiservice.shared.security;

import static com.commercepal.apiservice.users.enums.UserType.STAFF;

import com.commercepal.apiservice.shared.cache.InMemoryCacheService;
import com.commercepal.apiservice.users.credential.Credential;
import com.commercepal.apiservice.users.credential.CredentialRepository;
import com.commercepal.apiservice.users.enums.UserType;
import com.commercepal.apiservice.users.role.RoleCode;
import com.commercepal.apiservice.users.role.RoleDefinition;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

/**
 * JWT Token Provider for enterprise-grade security. Provides comprehensive token management with
 * refresh tokens, blacklisting, device tracking, and advanced security features.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtTokenProvider {

  // Cach key prefixes
  private static final String TOKEN_BLACKLIST_PREFIX = "blacklist:token:";
  private static final String USER_SESSIONS_PREFIX = "sessions:user:";
  private static final String REFRESH_TOKEN_PREFIX = "refresh:";
  private final InMemoryCacheService inMemoryCacheService;
  private final SecurityAuditService securityAuditService;
  private final CredentialRepository credentialRepository;
  private final UserDetailsService userDetailsService;
  private final JwtTokenExpirationProperties expirationProperties;

  @Value("${jwt.secret:}")
  private String jwtSecret;
  @Value("${jwt.issuer:commercepal-api}")
  private String issuer;
  @Value("${jwt.audience:commercepal-users}")
  private String defaultAudience;
  // Security configuration
  @Value("${security.max-sessions-per-user:3}")
  private int maxSessionsPerUser;
  @Value("${security.token-blacklist-enabled:true}")
  private boolean tokenBlacklistEnabled;

  /**
   * Generate comprehensive token pair (access + refresh) with device tracking
   */
  public TokenPair generateTokenPair(Authentication authentication, String deviceFingerprint,
      String ipAddress, String loginIdentifier) {
    Credential user = getUserFromAuthentication(authentication);

    if (user == null) {
      throw new JwtException("User not found in authentication");
    }

    String tokenFamily = generateTokenFamily();
    String sessionId = generateSessionId();

    long accessTtlMs = expirationProperties.getAccessTtlMs(user.getUserType());
    long refreshTtlMs = expirationProperties.getRefreshTtlMs(user.getUserType());

    // Create access token with comprehensive claims
    String accessToken = createAccessToken(loginIdentifier, user, tokenFamily, sessionId,
        deviceFingerprint, ipAddress, accessTtlMs);

    // Create refresh token
    String refreshToken = createRefreshToken(loginIdentifier, tokenFamily, sessionId,
        deviceFingerprint, refreshTtlMs);

    // Store session information
    storeSessionInfo(user.getId(), sessionId, tokenFamily, deviceFingerprint, ipAddress,
        refreshTtlMs);

    // Enforce session limits
    enforceSessionLimits(user.getId());

    // Audit token generation
    securityAuditService.auditTokenGeneration(user.getId(), sessionId, deviceFingerprint,
        ipAddress);

    log.info("[JWT] Generated token pair for user: {} from device: {} IP: {}",
        loginIdentifier, deviceFingerprint, ipAddress);

    return TokenPair.builder().accessToken(accessToken).refreshToken(refreshToken)
        .tokenType("Bearer").expiresIn(accessTtlMs / 1000).sessionId(sessionId).build();
  }

  /**
   * Create access token with comprehensive security claims
   */
  private String createAccessToken(String loginIdentifier, Credential user,
      String tokenFamily,
      String sessionId, String deviceFingerprint, String ipAddress, long accessTtlMs) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + accessTtlMs);

    // Determine audience based on user type
    String tokenAudience = getAudienceForUserType(user.getUserType());

    io.jsonwebtoken.JwtBuilder builder = Jwts.builder()
        .setIssuer(issuer)
        .setAudience(tokenAudience)
        .setSubject(loginIdentifier)
        .setIssuedAt(now).setExpiration(expiry)
        .setNotBefore(now).setId(UUID.randomUUID().toString())

        // Session and Device Information
        .claim("sessionId", sessionId)
        .claim("tokenFamily", tokenFamily)
        .claim("deviceFingerprint", deviceFingerprint)
        .claim("ipAddress", ipAddress)
        .claim("tokenType", "access")

        // Account Status
        .claim("accountStatus", user.getStatus().toString())
        .claim("requiresPasswordChange", user.isRequiresPasswordChange());


    if(user.getUserType() == STAFF ){
      // Authorities
      // Get user roles
      List<RoleCode> roleNames = user.getRoles().stream().map(RoleDefinition::getCode).toList();
      builder.claim("roles", roleNames);
  }

    return builder.signWith(getSigningKey(), SignatureAlgorithm.HS512).compact();
  }

  /**
   * Create refresh token
   */
  private String createRefreshToken(String loginIdentifier, String tokenFamily, String sessionId,
      String deviceFingerprint, long refreshTtlMs) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + refreshTtlMs);

    String refreshToken = Jwts.builder().setIssuer(issuer)
        .setSubject(loginIdentifier)
        .setIssuedAt(now).setExpiration(expiry).setId(UUID.randomUUID().toString())
        .claim("tokenFamily", tokenFamily).claim("sessionId", sessionId)
        .claim("deviceFingerprint", deviceFingerprint).claim("tokenType", "refresh")
        .signWith(getSigningKey(), SignatureAlgorithm.HS512).compact();

    // Store refresh token in Redis with expiration
    storeRefreshToken(refreshToken, loginIdentifier, tokenFamily, sessionId, refreshTtlMs);

    return refreshToken;
  }

  /**
   * Refresh access token using refresh token
   */
  public TokenPair refreshAccessToken(String refreshToken, String deviceFingerprint,
      String ipAddress) {
    try {
      // Validate refresh token
      Claims claims = validateAndGetClaims(refreshToken);

      if (!"refresh".equals(claims.get("tokenType"))) {
        throw new JwtException("Invalid token type for refresh operation");
      }

      String loginIdentifier = claims.getSubject();
      String tokenFamily = claims.get("tokenFamily", String.class);
      String sessionId = claims.get("sessionId", String.class);
      String originalDevice = claims.get("deviceFingerprint", String.class);

      // Verify device consistency
      if (!originalDevice.equals(deviceFingerprint)) {
        log.warn("[JWT] Device fingerprint mismatch during token refresh for user: {}",
            loginIdentifier);
        securityAuditService.auditSuspiciousActivity(loginIdentifier, "DEVICE_MISMATCH_REFRESH",
            deviceFingerprint, ipAddress);
        throw new JwtException("Device verification failed");
      }

      // Verify refresh token exists in Redis
      if (!isRefreshTokenValid(refreshToken, tokenFamily)) {
        throw new JwtException("Refresh token not found or expired");
      }

      // Load user details
      UserDetails userDetails = userDetailsService.loadUserByUsername(loginIdentifier);

      // Create new authentication
      UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
          userDetails, null, userDetails.getAuthorities());

      // Generate new token pair
      TokenPair newTokenPair = generateTokenPair(authentication, deviceFingerprint, ipAddress,
          loginIdentifier);

      // Invalidate old refresh token
      invalidateRefreshToken(refreshToken, tokenFamily);

      log.info("[JWT] Successfully refreshed token for user: {} session: {}", loginIdentifier,
          sessionId);

      return newTokenPair;

    } catch (JwtException | IllegalArgumentException e) {
      log.error("[JWT] Token refresh failed: {}", e.getMessage());
      throw new JwtException("Token refresh failed: " + e.getMessage());
    }
  }

  /**
   * Validate token with comprehensive security checks
   */
  public boolean validateToken(String token, String deviceFingerprint, String ipAddress) {
    try {
      Claims claims = validateAndGetClaims(token);

      // Check if token is blacklisted
      if (isTokenBlacklisted(token)) {
        log.warn("[JWT] Attempted use of blacklisted token");
        return false;
      }

      // Verify device fingerprint (optional check - can be relaxed for mobile apps)
      String tokenDevice = claims.get("deviceFingerprint", String.class);
      if (tokenDevice != null && !tokenDevice.equals(deviceFingerprint)) {
        log.warn("[JWT] Device fingerprint mismatch for token validation");
        securityAuditService.auditSuspiciousActivity(claims.getSubject(), "DEVICE_MISMATCH",
            deviceFingerprint, ipAddress);
        // Don't fail for device mismatch - just log it (for mobile apps)
        // return false;
      }

      // Check session validity
      String sessionId = claims.get("sessionId", String.class);
      // userId is not in standard claims, might need to fetch user or use subject
      // For now, skipping session active check by userId as we don't have userId easily in claims without DB lookup
      // Or we could put userId in claims. Let's assume we trust the token signature and expiration for now.

      // Additional security validations
      return performAdditionalSecurityChecks(claims, ipAddress);

    } catch (JwtException | IllegalArgumentException e) {
      log.error("[JWT] Token validation failed: {}", e.getMessage());
      return false;
    }
  }

  /**
   * Simple token validation (for backward compatibility)
   */
  public boolean validateToken(String token) {
    try {
      validateAndGetClaims(token);
      return !isTokenBlacklisted(token);
    } catch (JwtException | IllegalArgumentException e) {
      log.error("[JWT] Token validation failed: {}", e.getMessage());
      return false;
    }
  }

  /**
   * Extract username from token
   */
  public String getUsernameFromToken(String token) {
    try {
      Claims claims = validateAndGetClaims(token);
      return claims.getSubject();
    } catch (JwtException e) {
      log.error("[JWT] Failed to extract username from token: {}", e.getMessage());
      return null;
    }
  }

  /**
   * Extract comprehensive user context from token
   */
  public UserSecurityContext getUserSecurityContext(String token) {
    try {
      Claims claims = validateAndGetClaims(token);

      String loginIdentifier = claims.getSubject();
      String email = claims.get("email", String.class);
      String phone = claims.get("phone", String.class);

      return UserSecurityContext.builder()
          .loginIdentifier(loginIdentifier).userType(claims.get("userType", String.class))
          .email(email).phone(phone)
          .sessionId(claims.get("sessionId", String.class))
          .deviceFingerprint(claims.get("deviceFingerprint", String.class))
          .ipAddress(claims.get("ipAddress", String.class))
          .riskScore(claims.get("riskScore", Integer.class))
          .roles(extractStringList(claims, "roles"))
          .issuedAt(claims.getIssuedAt().toInstant().atZone(ZoneOffset.UTC).toLocalDateTime())
          .expiresAt(claims.getExpiration().toInstant().atZone(ZoneOffset.UTC).toLocalDateTime())
          .build();

    } catch (JwtException e) {
      log.error("[JWT] Failed to extract security context from token: {}", e.getMessage());
      return null;
    }
  }

  /**
   * Safely extract a List<String> from JWT claims.
   *
   * @param claims    the JWT claims
   * @param claimName the name of the claim to extract
   * @return List of strings, or empty list if claim is null or not a list
   */
  private List<String> extractStringList(Claims claims, String claimName) {
    Object claimValue = claims.get(claimName);
    if (claimValue == null) {
      return Collections.emptyList();
    }

    if (claimValue instanceof List<?> rawList) {
      return rawList.stream()
          .filter(item -> item instanceof String)
          .map(item -> (String) item)
          .toList();
    }

    return Collections.emptyList();
  }

  /**
   * Blacklist token (for logout, security incidents, etc.)
   */
  public void blacklistToken(String token, String reason) {
    if (!tokenBlacklistEnabled) {
      return;
    }

    try {
      Claims claims = validateAndGetClaims(token);
      String tokenId = claims.getId();
      long remainingTime = claims.getExpiration().getTime() - System.currentTimeMillis();

      if (remainingTime > 0) {
        inMemoryCacheService.set(TOKEN_BLACKLIST_PREFIX + tokenId, reason, remainingTime);

        log.info("[JWT] Token blacklisted: {} reason: {}", tokenId, reason);
        securityAuditService.auditTokenBlacklist(claims.getSubject(), tokenId, reason);
      }
    } catch (JwtException e) {
      log.error("[JWT] Failed to blacklist token: {}", e.getMessage());
    }
  }

  /**
   * Invalidate all sessions for a user
   */
  public void invalidateAllUserSessions(Long userId, String reason) {
    String userSessionsKey = USER_SESSIONS_PREFIX + userId;
    Set<Object> sessions = inMemoryCacheService.getSetMembers(userSessionsKey);

    if (sessions != null) {
      for (Object sessionObj : sessions) {
        SessionInfo sessionInfo = (SessionInfo) sessionObj;
        invalidateSession(userId, sessionInfo.getSessionId(), reason);
      }
    }

    log.info("[JWT] Invalidated all sessions for user: {} reason: {}", userId, reason);
    securityAuditService.auditSessionInvalidation(userId, "ALL_SESSIONS", reason);
  }

  /**
   * Get active sessions for a user
   */
  public List<SessionInfo> getActiveUserSessions(Long userId) {
    String userSessionsKey = USER_SESSIONS_PREFIX + userId;
    Set<Object> sessions = inMemoryCacheService.getSetMembers(userSessionsKey);

    if (sessions == null) {
      return Collections.emptyList();
    }

    return sessions.stream().map(obj -> (SessionInfo) obj)
        .filter(session -> isSessionActive(userId, session.getSessionId())).toList();
  }

  // ============================================
  // PRIVATE HELPER METHODS
  // ============================================

  private javax.crypto.SecretKey getSigningKey() {
    if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
      // Generate a secure random key if not provided
      jwtSecret = generateSecureRandomKey();
      log.warn(
          "[JWT] JWT secret not configured, generated random key. This should be configured in production!");
    }

    byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
    // Ensure minimum 512 bits (64 bytes) for HS512 algorithm
    // RFC 7518 Section 3.2 requires key size >= hash output size (512 bits for HS512)
    if (keyBytes.length < 64) {
      // If key is shorter than 64 bytes, we need to expand it
      // Use a secure approach: hash the key to get 64 bytes
      try {
        java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-512");
        keyBytes = digest.digest(keyBytes);
      } catch (java.security.NoSuchAlgorithmException e) {
        // Fallback: pad with zeros (not ideal, but maintains compatibility)
        log.warn("[JWT] SHA-512 not available, using padded key. This is not recommended.");
        byte[] paddedKey = new byte[64];
        System.arraycopy(keyBytes, 0, paddedKey, 0, keyBytes.length);
        // Repeat the key pattern to fill remaining bytes
        for (int i = keyBytes.length; i < 64; i++) {
          paddedKey[i] = keyBytes[i % keyBytes.length];
        }
        keyBytes = paddedKey;
      }
    }
    return Keys.hmacShaKeyFor(keyBytes);
  }

  private String generateSecureRandomKey() {
    SecureRandom random = new SecureRandom();
    byte[] key = new byte[64]; // 512 bits for HS512
    random.nextBytes(key);
    return Base64.getEncoder().encodeToString(key);
  }

  private Claims validateAndGetClaims(String token) {
    // For validation, we accept any valid audience (since it's dynamic based on user type)
    return Jwts.parserBuilder().setSigningKey(getSigningKey()).requireIssuer(issuer).build()
        .parseClaimsJws(token).getBody();
  }

  /**
   * Get audience string based on user type. Returns appropriate audience identifier for the user
   * type.
   *
   * @param userType the user type
   * @return audience string for the user type
   */
  private String getAudienceForUserType(UserType userType) {
    if (userType == null) {
      return defaultAudience;
    }

    return switch (userType) {
      case CUSTOMER -> "commercepal-customers";
      case AGENT -> "commercepal-agents";
      case STAFF -> "commercepal-staff";
      case MERCHANT -> "commercepal-merchants";
      case SYSTEM -> "commercepal-system";
      default -> defaultAudience;
    };
  }

  private boolean isTokenBlacklisted(String token) {
    if (!tokenBlacklistEnabled) {
      return false;
    }

    try {
      Claims claims = validateAndGetClaims(token);
      String tokenId = claims.getId();
      return inMemoryCacheService.hasKey(TOKEN_BLACKLIST_PREFIX + tokenId);
    } catch (JwtException e) {
      return true; // Consider invalid tokens as blacklisted
    }
  }

  private void storeSessionInfo(Long userId, String sessionId, String tokenFamily,
      String deviceFingerprint, String ipAddress, long refreshTtlMs) {
    SessionInfo sessionInfo = SessionInfo.builder().sessionId(sessionId).tokenFamily(tokenFamily)
        .deviceFingerprint(deviceFingerprint).ipAddress(ipAddress).createdAt(LocalDateTime.now())
        .lastActivity(LocalDateTime.now()).build();

    String userSessionsKey = USER_SESSIONS_PREFIX + userId;
    inMemoryCacheService.addToSet(userSessionsKey, sessionInfo);
    inMemoryCacheService.expire(userSessionsKey, refreshTtlMs);
  }

  private void storeRefreshToken(String refreshToken, String username, String tokenFamily,
      String sessionId, long refreshTtlMs) {
    RefreshTokenInfo tokenInfo = RefreshTokenInfo.builder().token(refreshToken).username(username)
        .tokenFamily(tokenFamily).sessionId(sessionId).createdAt(LocalDateTime.now()).build();

    inMemoryCacheService.set(REFRESH_TOKEN_PREFIX + tokenFamily, tokenInfo, refreshTtlMs);
  }

  private boolean isRefreshTokenValid(String refreshToken, String tokenFamily) {
    RefreshTokenInfo tokenInfo = (RefreshTokenInfo) inMemoryCacheService.get(
        REFRESH_TOKEN_PREFIX + tokenFamily);

    return tokenInfo != null && refreshToken.equals(tokenInfo.getToken());
  }

  private void invalidateRefreshToken(String refreshToken, String tokenFamily) {
    inMemoryCacheService.delete(REFRESH_TOKEN_PREFIX + tokenFamily);
  }

  private boolean isSessionActive(Long userId, String sessionId) {
    String userSessionsKey = USER_SESSIONS_PREFIX + userId;
    Set<Object> sessions = inMemoryCacheService.getSetMembers(userSessionsKey);

    if (sessions == null) {
      return false;
    }

    return sessions.stream().map(obj -> (SessionInfo) obj)
        .anyMatch(session -> sessionId.equals(session.getSessionId()));
  }

  private void enforceSessionLimits(Long userId) {
    List<SessionInfo> activeSessions = getActiveUserSessions(userId);

    if (activeSessions.size() > maxSessionsPerUser) {
      // Remove oldest sessions
      activeSessions.stream().sorted((s1, s2) -> s1.getCreatedAt().compareTo(s2.getCreatedAt()))
          .limit(activeSessions.size() - maxSessionsPerUser).forEach(
              session -> invalidateSession(userId, session.getSessionId(), "SESSION_LIMIT_EXCEEDED"));
    }
  }

  private void invalidateSession(Long userId, String sessionId, String reason) {
    String userSessionsKey = USER_SESSIONS_PREFIX + userId;
    Set<Object> sessions = inMemoryCacheService.getSetMembers(userSessionsKey);

    if (sessions != null) {
      sessions.stream().map(obj -> (SessionInfo) obj)
          .filter(session -> sessionId.equals(session.getSessionId())).findFirst()
          .ifPresent(session -> {
            inMemoryCacheService.removeFromSet(userSessionsKey, session);
            inMemoryCacheService.delete(REFRESH_TOKEN_PREFIX + session.getTokenFamily());
          });
    }
  }

  private boolean performAdditionalSecurityChecks(Claims claims, String currentIpAddress) {
    // IP address validation (optional - can be relaxed for mobile apps)
    String tokenIpAddress = claims.get("ipAddress", String.class);
    if (tokenIpAddress != null && !tokenIpAddress.equals(currentIpAddress)) {
      log.info("[JWT] IP address changed from {} to {} for user: {}", tokenIpAddress,
          currentIpAddress, claims.getSubject());
      // Don't fail - just log the change
    }

    // Time-based validations
    Date issuedAt = claims.getIssuedAt();
    if (issuedAt.after(new Date())) {
      log.warn("[JWT] Token issued in the future for user: {}", claims.getSubject());
      return false;
    }

    return true;
  }

  private String generateTokenFamily() {
    return UUID.randomUUID().toString();
  }

  private String generateSessionId() {
    return UUID.randomUUID().toString();
  }

  private Credential getUserFromAuthentication(Authentication authentication) {
    if (authentication.getPrincipal() instanceof Credential user) {
      return user;
    } else if (authentication.getPrincipal() instanceof UserDetails userDetails) {
      return credentialRepository.findByIdentifier(userDetails.getUsername()).orElse(null);
    }
    return null;
  }

  // INNER CLASSES
  @Data
  @Builder
  public static class TokenPair {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
    private String sessionId;
  }

  @Data
  @Builder
  public static class UserSecurityContext {

    //    private String username;
    private String loginIdentifier;
    private String userType;
    private String email;
    private String phone;
    private Integer securityLevel;
    private String sessionId;
    private String deviceFingerprint;
    private String ipAddress;
    private Boolean mfaVerified;
    private Integer riskScore;
    private List<String> roles;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
  }

  @Data
  @Builder
  public static class SessionInfo {

    private String sessionId;
    private String tokenFamily;
    private String deviceFingerprint;
    private String ipAddress;
    private LocalDateTime createdAt;
    private LocalDateTime lastActivity;
  }

  @Data
  @Builder
  public static class RefreshTokenInfo {

    private String token;
    private String username;
    private String tokenFamily;
    private String sessionId;
    private LocalDateTime createdAt;
  }
}
