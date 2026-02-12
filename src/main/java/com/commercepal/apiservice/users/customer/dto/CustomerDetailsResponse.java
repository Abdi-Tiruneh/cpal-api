package com.commercepal.apiservice.users.customer.dto;

import com.commercepal.apiservice.shared.enums.Channel;
import com.commercepal.apiservice.shared.enums.SupportedCurrency;
import com.commercepal.apiservice.users.enums.IdentityProvider;
import com.commercepal.apiservice.users.enums.UserStatus;
import com.commercepal.apiservice.users.role.RoleCode;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

/**
 * Complete customer details response DTO for admin detailed view.
 * <p>
 * This record provides comprehensive customer information in a hierarchical structure, combining
 * all data from Customer and Credential entities. Organized hierarchically for detailed
 * admin views when retrieving customer by ID.
 */
@Builder
@Schema(
    name = "CustomerDetailsResponse",
    description = """
        Complete customer details response for admin detailed view.
        Contains all customer information in a hierarchical structure,
        combining complete data from Customer and AccountCredential entities.
        """
)
public record CustomerDetailsResponse(

    @Schema(description = "Customer identification and basic information")
    CustomerInfo customerInfo,

    @Schema(description = "Account credential and authentication information")
    AccountCredentialInfo accountCredentialInfo,

    @Schema(description = "Audit and metadata information")
    AuditInfo auditInfo

) {

  /**
   * Customer information section.
   */
  @Builder
  @Schema(description = "Customer identification and profile information")
  public record CustomerInfo(

      @Schema(description = "Unique identifier for the customer record", example = "1")
      Long id,

      @Schema(description = "Unique account number assigned to the customer", example = "CUS2024000001")
      String accountNumber,

      @Schema(description = "Commission account number associated with the customer", example = "CUS20240000011")
      String commissionAccount,

      @Schema(description = "Customer's first name", example = "John")
      String firstName,

      @Schema(description = "Customer's last name", example = "Doe")
      String lastName,

      @Schema(description = "Country of the customer (ISO 3166-1 alpha-2 code)", example = "ET")
      String country,

      @Schema(description = "City where the customer is located", example = "Addis Ababa")
      String city,

      @Schema(description = "State or province where the customer is located", example = "Addis Ababa")
      String stateProvince,

      @Schema(description = "Customer's preferred language (ISO 639-1 code)", example = "en")
      String preferredLanguage,

      @Schema(description = "Customer's preferred currency (SupportedCurrency)", example = "ETB")
      SupportedCurrency preferredCurrency,

      @Schema(description = "Unique referral code assigned to the customer", example = "REF123456")
      String referralCode,

      @Schema(description = "Channel through which the customer registered", example = "WEB")
      Channel registrationChannel,

      @Schema(description = "Customer notes (visible to customer)", example = "Preferred contact method: SMS")
      String customerNotes,

      @Schema(description = "Admin notes (internal use only)", example = "VIP customer")
      String adminNotes

  ) {

  }

  /**
   * Account credential information section.
   */
  @Builder
  @Schema(description = "Account credential and authentication information")
  public record AccountCredentialInfo(

      @Schema(description = "Unique identifier for the account credential", example = "1")
      Long id,

      @Schema(description = "Email address associated with the account", example = "john.doe@example.com")
      String emailAddress,

      @Schema(description = "Phone number associated with the account", example = "+251912345678")
      String phoneNumber,

      @Schema(description = "Account status", example = "ACTIVE")
      UserStatus status,

      @Schema(description = "Whether the email is verified", example = "true")
      Boolean isEmailVerified,

      @Schema(description = "Whether the phone is verified", example = "true")
      Boolean isPhoneVerified,

      @Schema(description = "Timestamp when email was verified", example = "2024-01-15T10:30:00")
      LocalDateTime emailVerifiedAt,

      @Schema(description = "Timestamp when phone was verified", example = "2024-01-15T10:30:00")
      LocalDateTime phoneVerifiedAt,

      @Schema(description = "Number of failed sign-in attempts", example = "0")
      Integer failedSignInAttempts,

      @Schema(description = "Timestamp until which the account is locked", example = "2024-01-20T14:45:00")
      LocalDateTime lockedUntil,

      @Schema(description = "Timestamp of last successful sign-in", example = "2024-01-20T14:45:00")
      LocalDateTime lastSignedInAt,

      @Schema(description = "Timestamp of last failed sign-in attempt", example = "2024-01-19T10:00:00")
      LocalDateTime lastFailedSignIn,

      @Schema(description = "Last access channel used", example = "WEB")
      Channel lastAccessChannel,

      @Schema(description = "Timestamp of last password change", example = "2024-01-15T10:30:00")
      LocalDateTime lastPasswordChangeAt,

      @Schema(description = "Whether password reset token is expired", example = "true")
      Boolean isPasswordResetTokenExpired,

      @Schema(description = "Whether password change is required", example = "false")
      Boolean requiresPasswordChange,

      @Schema(description = "Whether multi-factor authentication is enabled", example = "false")
      Boolean mfaEnabled,

      @Schema(description = "Identity provider used for authentication", example = "LOCAL")
      IdentityProvider identityProvider,

      @Schema(description = "Device ID for mobile applications", example = "device-123")
      String deviceId,

      @Schema(description = "Notification token for push notifications", example = "token-123")
      String notificationToken,

      @Schema(description = "List of roles assigned to the account")
      List<RoleInfo> roles

  ) {

  }

  /**
   * Role information.
   */
  @Builder
  @Schema(description = "Role information")
  public record RoleInfo(

      @Schema(description = "Unique identifier for the role", example = "1")
      Long id,

      @Schema(description = "Role code", example = "ROLE_CUSTOMER")
      RoleCode code,

      @Schema(description = "Role name", example = "Customer")
      String name,

      @Schema(description = "Role description", example = "Standard customer role")
      String description,

      @Schema(description = "Whether the role is active", example = "true")
      Boolean active

  ) {

  }

  /**
   * Audit and metadata information section.
   */
  @Builder
  @Schema(description = "Audit trail and metadata information")
  public record AuditInfo(

      @Schema(description = "Customer record creation timestamp", example = "2024-01-15T10:30:00")
      LocalDateTime customerCreatedAt,

      @Schema(description = "Customer record last update timestamp", example = "2024-01-20T14:45:00")
      LocalDateTime customerUpdatedAt,

      @Schema(description = "User who created the customer record", example = "SYSTEM")
      String customerCreatedBy,

      @Schema(description = "User who last updated the customer record", example = "admin@example.com")
      String customerUpdatedBy,

      @Schema(description = "IP address from which customer was created", example = "192.168.1.1")
      String customerCreatedIp,

      @Schema(description = "IP address from which customer was last updated", example = "192.168.1.1")
      String customerUpdatedIp,

      @Schema(description = "Version number for optimistic locking", example = "1")
      Long customerVersion,

      @Schema(description = "Whether customer record is deleted", example = "false")
      Boolean customerIsDeleted,

      @Schema(description = "Timestamp when customer was deleted", example = "2024-01-25T10:00:00")
      LocalDateTime customerDeletedAt,

      @Schema(description = "User who deleted the customer record", example = "admin@example.com")
      String customerDeletedBy,

      @Schema(description = "Remarks on customer record", example = "VIP customer")
      String customerRemarks,

      @Schema(description = "Account credential creation timestamp", example = "2024-01-15T10:30:00")
      LocalDateTime credentialCreatedAt,

      @Schema(description = "Account credential last update timestamp", example = "2024-01-20T14:45:00")
      LocalDateTime credentialUpdatedAt,

      @Schema(description = "User who created the account credential", example = "SYSTEM")
      String credentialCreatedBy,

      @Schema(description = "User who last updated the account credential", example = "admin@example.com")
      String credentialUpdatedBy,

      @Schema(description = "Version number for optimistic locking", example = "1")
      Long credentialVersion,

      @Schema(description = "Whether account credential is deleted", example = "false")
      Boolean credentialIsDeleted

  ) {

  }

}
