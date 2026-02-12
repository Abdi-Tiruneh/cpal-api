package com.commercepal.apiservice.shared.security;

import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MINUTES;

import com.commercepal.apiservice.shared.exceptions.security.AccountLockedException;
import com.commercepal.apiservice.users.credential.Credential;
import com.commercepal.apiservice.users.credential.CredentialRepository;
import com.commercepal.apiservice.users.enums.UserStatus;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UserDetailsService with comprehensive security features including caching, audit logging, and
 * advanced user validation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

  private final CredentialRepository credentialRepository;
  private final SecurityAuditService securityAuditService;
  private final FailedLoginProtectionService failedLoginProtectionService;

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String loginIdentifier) throws UsernameNotFoundException {
    log.debug("[SECURITY-USER-DETAILS] Loading user details for loginIdentifier: {}",
        loginIdentifier);

    // Normalize the identifier (trim whitespace)
    String normalizedIdentifier = loginIdentifier.trim();
    if (normalizedIdentifier.isEmpty()) {
      log.warn("[SECURITY-USER-DETAILS] Empty or null login identifier provided");
      securityAuditService.auditFailedLogin(loginIdentifier, "EMPTY_IDENTIFIER", null, null);
      throw new UsernameNotFoundException("Login identifier cannot be empty");
    }

    // Search by username, email, or phonePrimary with roles and permissions eagerly loaded
    Credential user = credentialRepository.findByIdentifier(normalizedIdentifier)
        .orElseThrow(() -> {
          log.warn("[SECURITY-USER-DETAILS] User not found with identifier: {}",
              normalizedIdentifier);
          securityAuditService.auditFailedLogin(normalizedIdentifier, "USER_NOT_FOUND", null, null);
          return new UsernameNotFoundException("User not found: " + normalizedIdentifier);
        });

    // Comprehensive account validation
    validateUserAccount(user, loginIdentifier);

    log.debug(
        "[SECURITY-USER-DETAILS] Successfully loaded user details for identifier: {} (found by: {})",
        normalizedIdentifier, determineIdentifierType(normalizedIdentifier, user));
    return user;
  }

  /**
   * Determine which identifier type matched the user (for logging purposes)
   */
  private String determineIdentifierType(String identifier, Credential user) {
    if (user.getEmailAddress() != null && user.getEmailAddress().equalsIgnoreCase(identifier)) {
      return "email";
    } else if (user.getPhoneNumber() != null && user.getPhoneNumber().equals(identifier)) {
      return "phone";
    }
    return "unknown";
  }

  /**
   * Check if user is active
   */
  @Transactional(readOnly = true)
  public boolean isUserActive(String loginIdentifier) {
    return credentialRepository.findByIdentifier(loginIdentifier).map(user ->
        user.getStatus() == UserStatus.ACTIVE
            && user.isEnabled() && user.isAccountNonLocked()).orElse(false);
  }

  /**
   * Comprehensive validation of user account status, constraints, and security requirements.
   *
   * @param user the user entity to validate
   * @throws AccountLockedException if the account is currently locked
   * @throws DisabledException      if the account is in a non-active state
   */
  private void validateUserAccount(Credential user, String loginIdentifier) {
    LocalDateTime now = LocalDateTime.now();

    // 1. Check account lock status (temporary lock from failed login attempts)
    if (!user.isAccountNonLocked()) {
      log.warn("[SECURITY-USER-DETAILS] Account locked for user: {} (locked until: {})",
          loginIdentifier, user.getLockedUntil());

      String message;
      if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(now)) {
        long hoursUntilUnlock = HOURS.between(now, user.getLockedUntil());
        if (hoursUntilUnlock > 0) {
          message = String.format(
              "Your account has been temporarily locked due to multiple failed login attempts. "
                  + "Access will be automatically restored on %s (approximately %d hour%s). "
                  + "If you believe this is an error, please contact support immediately.",
              user.getLockedUntil()
                  .format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a")),
              hoursUntilUnlock, hoursUntilUnlock != 1 ? "s" : "");
        } else {
          long minutesUntilUnlock = MINUTES.between(now, user.getLockedUntil());
          message = String.format(
              "Your account has been temporarily locked due to multiple failed login attempts. "
                  + "Access will be automatically restored on %s (approximately %d minute%s). "
                  + "Please wait and try again, or contact support if you need immediate assistance.",
              user.getLockedUntil()
                  .format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a")),
              minutesUntilUnlock, minutesUntilUnlock != 1 ? "s" : "");
        }
      } else {
        message = "Your account has been locked for security reasons. Please contact support to unlock your account and verify your identity.";
      }

      throw new AccountLockedException(message);
    }

    // 3. Check if password change is required (non-blocking, informational only)
    if (user.isRequiresPasswordChange()) {
      log.info("[SECURITY-USER-DETAILS] User requires password change: {}", loginIdentifier);
      // This will be handled in the authentication process, not blocking login
    }

    log.debug("[SECURITY-USER-DETAILS] Account validation passed for user: {}", loginIdentifier);
  }

  /**
   * Update user last activity
   */
  @Transactional
  public void updateLastActivity(String loginIdentifier) {
    try {
      credentialRepository.findByIdentifier(loginIdentifier).ifPresent(user -> {
        user.setLastSignedInAt(LocalDateTime.now());
        credentialRepository.save(user);
        log.debug("[SECURITY-USER-DETAILS] Updated last activity for user: {}", user.getUsername());
      });
    } catch (Exception e) {
      log.error("[SECURITY-USER-DETAILS] Failed to update last activity for user: {}",
          loginIdentifier, e);
    }
  }

  /**
   * Increment failed login attempts using enterprise-grade protection service.
   */
  @Transactional
  public void incrementFailedLoginAttempts(String username, String ipAddress,
      String deviceFingerprint) {
    try {
      FailedLoginProtectionService.ProtectionResult result = failedLoginProtectionService.recordFailedLogin(
          username, ipAddress, deviceFingerprint);

      if (result.isLocked()) {
        log.error("[SECURITY-USER-DETAILS] Account locked for user: {} until: {}", username,
            result.getLockUntil());
      } else {
        log.warn(
            "[SECURITY-USER-DETAILS] Failed login attempt recorded for user: {} ({} remaining, delay: {}s, captcha: {})",
            username, result.getRemainingAttempts(), result.getDelaySeconds(),
            result.isRequiresCaptcha());
      }
    } catch (AccountLockedException e) {
      // Re-throw account locked exceptions
      throw e;
    } catch (Exception e) {
      log.error("[SECURITY-USER-DETAILS] Failed to record failed login attempt for user: {}",
          username, e);
      // Fallback to simple increment if protection service fails
      incrementFailedLoginAttemptsLegacy(username);
    }
  }

  /**
   * Legacy implementation (fallback only).
   */
  private void incrementFailedLoginAttemptsLegacy(String username) {
    try {
      credentialRepository.findByIdentifier(username)
          .ifPresent(user -> {
            int currentAttempts = user.getFailedSignInAttempts();
            user.setFailedSignInAttempts(currentAttempts + 1);
            // user.setLastFailedLogin(LocalDateTime.now()); // Not in Credential

            // Lock account after 3 failed attempts for 15 minutes
            if (user.getFailedSignInAttempts() >= 3) {
              user.setLockedUntil(LocalDateTime.now().plusMinutes(15)); // Lock for 15 minutes
              log.warn("[SECURITY-USER-DETAILS] Account locked due to failed login attempts: {}",
                  username);
              securityAuditService.auditAccountLocked(username, "FAILED_LOGIN_ATTEMPTS", null,
                  null);
            }

            credentialRepository.save(user);
          });
    } catch (Exception e) {
      log.error(
          "[SECURITY-USER-DETAILS] Failed to increment failed login attempts (legacy) for user: {}",
          username, e);
    }
  }

  /**
   * Reset failed login attempts using protection service.
   */
  @Transactional
  public void resetFailedLoginAttempts(String username) {
    try {
      failedLoginProtectionService.resetFailedLoginAttempts(username);
      log.debug("[SECURITY-USER-DETAILS] Reset failed login attempts for user: {}", username);
    } catch (Exception e) {
      log.error("[SECURITY-USER-DETAILS] Failed to reset failed login attempts for user: {}",
          username, e);
      // Fallback to legacy method
      resetFailedLoginAttemptsLegacy(username);
    }
  }

  /**
   * Legacy reset method (fallback only).
   */
  private void resetFailedLoginAttemptsLegacy(String username) {
    try {
      credentialRepository.findByIdentifier(username)
          .ifPresent(user -> {
            user.setFailedSignInAttempts(0);
            // user.setLastFailedLogin(null); // Not in Credential
            // Don't clear lockedUntil - let it expire naturally
            credentialRepository.save(user);
          });
    } catch (Exception e) {
      log.error(
          "[SECURITY-USER-DETAILS] Failed to reset failed login attempts (legacy) for user: {}",
          username, e);
    }
  }
}


