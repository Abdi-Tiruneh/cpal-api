package com.commercepal.apiservice.users.credential;

import com.commercepal.apiservice.shared.enums.Channel;
import com.commercepal.apiservice.users.enums.UserType;
import com.commercepal.apiservice.users.enums.IdentityProvider;
import com.commercepal.apiservice.users.enums.UserStatus;
import com.commercepal.apiservice.users.role.RoleDefinition;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "credentials",
    indexes = {
        @Index(name = "idx_credentials_status", columnList = "status"),
        @Index(name = "idx_credentials_email", columnList = "email_address"),
        @Index(name = "idx_credentials_phone", columnList = "phone_number"),
        @Index(name = "idx_credentials_provider_user", columnList = "identity_provider_user_id")
    }
)
public class Credential implements UserDetails {

  /* ===================== PRIMARY KEY ===================== */

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /* ===================== USER TYPE ===================== */

  @Enumerated(EnumType.STRING)
  @Column(name = "user_type", nullable = false, length = 16)
  private UserType userType;

  /* ===================== CONTACT INFO ===================== */

  @Column(name = "email_address", length = 254)
  private String emailAddress;

  @Column(name = "phone_number", length = 32)
  private String phoneNumber;

  /* ===================== AUTH CREDENTIALS ===================== */

  @Column(name = "password_hash", nullable = false, length = 255)
  private String passwordHash;

  @Builder.Default
  @Column(name = "has_password", nullable = false)
  private boolean hasPassword = true;

  @Enumerated(EnumType.STRING)
  @Column(name = "identity_provider", nullable = false, length = 32)
  @Builder.Default
  private IdentityProvider identityProvider = IdentityProvider.LOCAL;

  @Column(name = "identity_provider_user_id", length = 128)
  private String identityProviderUserId;

  /* ===================== ACCOUNT STATUS ===================== */

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 32)
  @Builder.Default
  private UserStatus status = UserStatus.ACTIVE;

  @Builder.Default
  @Column(name = "failed_sign_in_attempts", nullable = false)
  private int failedSignInAttempts = 0;

  @Column(name = "last_failed_sign_in_at")
  private LocalDateTime lastFailedSignInAt;

  @Column(name = "locked_until")
  private LocalDateTime lockedUntil;

  @Column(name = "last_signed_in_at")
  private LocalDateTime lastSignedInAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "last_access_channel", length = 16)
  private Channel lastAccessChannel;

  /**
   * Device identifier for the last successful sign-in.
   * Used for device tracking, security alerts, and session management.
   */
  @Column(name = "last_device_id", length = 128)
  private String lastDeviceId;

  /* ===================== VERIFICATION ===================== */

  @Builder.Default
  @Column(name = "email_verified", nullable = false)
  private boolean emailVerified = false;

  @Column(name = "email_verified_at")
  private LocalDateTime emailVerifiedAt;

  @Builder.Default
  @Column(name = "phone_verified", nullable = false)
  private boolean phoneVerified = false;

  @Column(name = "phone_verified_at")
  private LocalDateTime phoneVerifiedAt;

  /* ===================== MFA (FLAG ONLY) ===================== */

  /**
   * MFA secrets should live in a secured table/service.
   */
  @Builder.Default
  @Column(name = "mfa_enabled", nullable = false)
  private boolean mfaEnabled = false;

  /* ===================== PASSWORD MANAGEMENT ===================== */

  @Column(name = "last_password_change_at")
  private LocalDateTime lastPasswordChangeAt;

  @Builder.Default
  @Column(name = "requires_password_change", nullable = false)
  private boolean requiresPasswordChange = false;

  /* ===================== PASSWORD RESET ===================== */

  @Column(name = "password_reset_token", length = 10)
  private String passwordResetToken;

  @Column(name = "password_reset_expires_at")
  private LocalDateTime passwordResetExpiresAt;

  @Builder.Default
  @Column(name = "password_reset_failed_attempts", nullable = false)
  private int passwordResetFailedAttempts = 0;

  /* ===================== ROLES ===================== */

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "credential_roles",
      joinColumns = @JoinColumn(name = "credential_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id")
  )
  @Builder.Default
  private Set<RoleDefinition> roles = new HashSet<>();

  /* ===================== AUDIT ===================== */

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @CreatedBy
  @Column(name = "created_by", nullable = false, updatable = false, length = 100)
  private String createdBy;

  @LastModifiedBy
  @Column(name = "updated_by", length = 100)
  private String updatedBy;

  /* ===================== SOFT DELETE ===================== */

  @Builder.Default
  @Column(name = "is_deleted", nullable = false)
  private boolean deleted = false;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @Column(name = "deleted_by", length = 100)
  private String deletedBy;

  /* ===================== VERSION ===================== */

  @Version
  @Column(name = "version", nullable = false)
  private Long version;

  /* ===================== LIFECYCLE ===================== */

  @PrePersist
  void onCreate() {
    if (emailAddress == null && phoneNumber == null) {
      throw new IllegalStateException(
          "At least one of email address or phone number must be provided"
      );
    }
    this.createdAt = LocalDateTime.now();
  }

  /* ===================== DOMAIN LOGIC ===================== */

  public boolean isOAuthAccount() {
    return identityProvider != IdentityProvider.LOCAL;
  }

  public void recordSuccessfulLogin(Channel channel, String deviceId) {
    this.failedSignInAttempts = 0;
    this.lockedUntil = null;
    this.lastSignedInAt = LocalDateTime.now();
    this.lastAccessChannel = channel;
    this.lastDeviceId = deviceId;
  }

  public void recordFailedLogin(int maxAttempts, int lockMinutes) {
    this.failedSignInAttempts++;
    this.lastFailedSignInAt = LocalDateTime.now();
    if (failedSignInAttempts >= maxAttempts) {
      this.lockedUntil = LocalDateTime.now().plusMinutes(lockMinutes);
    }
  }

  public void softDelete(String deletedBy) {
    this.deleted = true;
    this.deletedAt = LocalDateTime.now();
    this.deletedBy = deletedBy;
    this.status = UserStatus.DEACTIVATED;
  }

  public void markEmailVerified() {
    this.emailVerified = true;
    this.emailVerifiedAt = LocalDateTime.now();
  }

  public void markPhoneVerified() {
    this.phoneVerified = true;
    this.phoneVerifiedAt = LocalDateTime.now();
  }

  public void updatePassword(String encodedPassword) {
    this.passwordHash = Objects.requireNonNull(encodedPassword);
    this.hasPassword = true;
  }

  /* ===================== PASSWORD RESET MANAGEMENT ===================== */

  public void markPasswordResetToken(String token, LocalDateTime expiresAt) {
    this.passwordResetToken = Objects.requireNonNull(token);
    this.passwordResetExpiresAt = Objects.requireNonNull(expiresAt);
    this.passwordResetFailedAttempts = 0;
  }

  public void clearPasswordResetToken() {
    this.passwordResetToken = null;
    this.passwordResetExpiresAt = null;
    this.passwordResetFailedAttempts = 0;
  }

  public void incrementPasswordResetFailedAttempts() {
    this.passwordResetFailedAttempts++;
  }

  public boolean isPasswordResetTokenExpired(LocalDateTime now) {
    return passwordResetExpiresAt == null || passwordResetExpiresAt.isBefore(now);
  }

  public boolean isPasswordResetTokenInvalidated() {
    return passwordResetFailedAttempts >= 3;
  }

  /* ===================== ROLE MANAGEMENT ===================== */

  public void assignRole(RoleDefinition role) {
    roles.add(Objects.requireNonNull(role));
  }

  public void revokeRole(RoleDefinition role) {
    roles.remove(Objects.requireNonNull(role));
  }

  /* ===================== UserDetails ===================== */

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return roles.stream()
        .map(RoleDefinition::getCode)
        .map(code -> new SimpleGrantedAuthority(code.name()))
        .collect(Collectors.toSet());
  }

  @Override
  public String getUsername() {
    return emailAddress != null ? emailAddress : phoneNumber;
  }

  @Override
  public String getPassword() {
    return passwordHash;
  }

  @Override
  public boolean isAccountNonLocked() {
    return lockedUntil == null || lockedUntil.isBefore(LocalDateTime.now());
  }

  @Override
  public boolean isEnabled() {
    return !deleted && status == UserStatus.ACTIVE;
  }
}
