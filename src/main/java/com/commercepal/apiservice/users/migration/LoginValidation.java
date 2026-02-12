package com.commercepal.apiservice.users.migration;

import com.commercepal.apiservice.shared.converter.UserStatusIntegerConverter;
import com.commercepal.apiservice.users.enums.SocialMedia;
import com.commercepal.apiservice.users.enums.UserRole;
import com.commercepal.apiservice.users.enums.UserStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "LoginValidation", indexes = {
    @Index(name = "idx_email_phone", columnList = "email, phone_number"),
    @Index(name = "idx_user_id", columnList = "user_id")})
public class LoginValidation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // FK reference to User
  @Column(name = "user_id", nullable = false)
  private Long userId;

  // User email (used for login or contact)
  @Column(name = "email")
  private String emailAddress;

  // User phone number (used for login or contact)
  @Column(name = "phone_number")
  private String phoneNumber;

  // Encrypted password (BCrypt or similar)
  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  // Device identifier (optional, for fraud detection or device-based login)
  @Column(name = "device_id")
  private String deviceId;

  // Number of failed login attempts (used to detect brute-force)
  @Column(name = "login_attempts")
  private final Integer loginAttempts = 0;

  // Lock time if too many failed login attempts
  @Column(name = "login_locked_until")
  private Instant loginLockedUntil;

  // Number of failed reset token attempts (used to detect brute-force)
  @Column(name = "reset_attempts")
  private final Integer resetAttempts = 0;

  // Timestamp of last login attempt
  @Column(name = "last_attempt_at")
  private Instant lastAttemptAt;

  // Flags to indicate if phone is verified
  @Column(name = "is_phone_verified")
  private final Boolean isPhoneVerified = false;

  // Flag to indicate if email is verified
  @Column(name = "is_email_verified")
  private final Boolean isEmailVerified = false;

  // OTP sent to phone (hashed)
  @Column(name = "otp_hash")
  private String otpHash;

  // OTP sent to email (hashed)
  @Column(name = "email_otp_hash")
  private String emailOtpHash;

  // Flag indicating OTP is required
  @Column(name = "otp_required")
  private final Boolean otpRequired = false;

  // Number of times OTP was resent
  @Column(name = "otp_resend_attempts")
  private final Integer otpResendAttempts = 0;

  // Timestamp of last OTP resend
  @Column(name = "last_otp_resend_at")
  private Instant lastOtpResendAt;

  // Password reset token
  @Column(name = "reset_token")
  private String resetToken;

//  // Expiration time of reset token
//  @Column(name = "reset_token_expiry")
//  private Instant resetTokenExpiry;

  // Notification token for push notifications (e.g., OneSignal/FCM)
  @Column(name = "notification_token")
  private String notificationToken;

  // OAuth provider if login is via social media
  @Enumerated(EnumType.STRING)
  @Column(name = "oauth_provider")
  private SocialMedia oauthProvider;

  // Unique ID from OAuth provider
  @Column(name = "oauth_provider_user_id")
  private String oauthProviderUserId;

  // User roles
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "UserRoles", joinColumns = @JoinColumn(name = "login_validation_id") // join on this table's PK
  )
  @Column(name = "role")
  @Enumerated(EnumType.STRING)
  private Set<UserRole> roles;


  // Account activation flag
  @Convert(converter = UserStatusIntegerConverter.class)
  @Column(name = "status", nullable = false)
  private UserStatus status;

  // Timestamp for entity creation
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  // Timestamp for last update
  @Column(name = "updated_at")
  private Instant updatedAt;

  public Collection<SimpleGrantedAuthority> getAuthorities() {
    return roles
        .stream()
        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
        .collect(Collectors.toSet());
  }

  public boolean isActive() {
    return this.status == UserStatus.ACTIVE;
  }

  @PrePersist
  public void prePersist() {
    if (emailAddress == null && phoneNumber == null) {
      throw new IllegalArgumentException("Either email address or phone number must be provided.");
    }
    Instant now = Instant.now();
    this.createdAt = now;
    this.updatedAt = now;
  }

  @PreUpdate
  public void preUpdate() {
    if (emailAddress == null && phoneNumber == null) {
      throw new IllegalArgumentException("Either email address or phone number must be provided.");
    }
    this.updatedAt = Instant.now();
  }
}

