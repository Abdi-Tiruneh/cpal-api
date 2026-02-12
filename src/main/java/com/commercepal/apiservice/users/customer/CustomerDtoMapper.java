package com.commercepal.apiservice.users.customer;

import com.commercepal.apiservice.users.customer.dto.CustomerDetailResponse;
import com.commercepal.apiservice.users.customer.dto.CustomerDetailsResponse;
import com.commercepal.apiservice.users.role.RoleDefinition;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper utility for converting Customer entities to DTOs.
 * <p>
 * This utility class provides static methods to map Customer entities to various DTOs using the
 * builder pattern for flexible object construction.
 */
public final class CustomerDtoMapper {

  private CustomerDtoMapper() {
    // Utility class - prevent instantiation
  }

  /**
   * Maps a Customer entity to CustomerDetailResponse (simple table view). Combines essential fields
   * from Customer and Credential in a flat structure.
   *
   * @param customer the customer entity
   * @return the customer detail response DTO, or null if customer is null
   */
  public static CustomerDetailResponse toDetailResponse(Customer customer) {
    if (customer == null) {
      return null;
    }

    String fullName = buildFullName(customer.getFirstName(), customer.getLastName());
    var credential = customer.getCredential();

    return CustomerDetailResponse.builder()
        .id(customer.getId())
        .accountNumber(customer.getAccountNumber())
        .fullName(fullName)
        .emailAddress(credential != null ? credential.getEmailAddress() : null)
        .phoneNumber(credential != null ? credential.getPhoneNumber() : null)
        .country(customer.getCountry())
        .city(customer.getCity())
        .preferredCurrency(customer.getPreferredCurrency())
        .status(credential != null ? credential.getStatus() : null)
        .isEmailVerified(credential != null ? credential.isEmailVerified() : null)
        .isPhoneVerified(credential != null ? credential.isPhoneVerified() : null)
        .registrationChannel(customer.getRegistrationChannel())
        .createdAt(customer.getCreatedAt())
        .lastSignedInAt(credential != null ? credential.getLastSignedInAt() : null)
        .build();
  }

  /**
   * Maps a Customer entity to CustomerDetailsResponse (hierarchical detailed view). Organizes all
   * data hierarchically from Customer and Credential.
   *
   * @param customer the customer entity
   * @return the customer details response DTO, or null if customer is null
   */
  public static CustomerDetailsResponse toDetailsResponse(Customer customer) {
    if (customer == null) {
      return null;
    }

    return CustomerDetailsResponse.builder()
        .customerInfo(buildCustomerInfo(customer))
        .accountCredentialInfo(buildAccountCredentialInfo(customer))
        .auditInfo(buildAuditInfo(customer))
        .build();
  }

  /**
   * Builds CustomerInfo from Customer entity.
   */
  private static CustomerDetailsResponse.CustomerInfo buildCustomerInfo(Customer customer) {
    return CustomerDetailsResponse.CustomerInfo.builder()
        .id(customer.getId())
//        .domainUserId(customer.getDomainUserId())
        .accountNumber(customer.getAccountNumber())
        .commissionAccount(customer.getCommissionAccount())
        .firstName(customer.getFirstName())
        .lastName(customer.getLastName())
        .country(customer.getCountry())
        .city(customer.getCity())
        .stateProvince(customer.getStateProvince())
        .preferredLanguage(customer.getPreferredLanguage())
        .preferredCurrency(customer.getPreferredCurrency())
        .referralCode(customer.getReferralCode())
        .registrationChannel(customer.getRegistrationChannel())
        .customerNotes(customer.getCustomerNotes())
        .adminNotes(customer.getAdminNotes())
        .build();
  }

  /**
   * Builds AccountCredentialInfo from Customer entity.
   */
  private static CustomerDetailsResponse.AccountCredentialInfo buildAccountCredentialInfo(
      Customer customer) {
    var credential = customer.getCredential();
    if (credential == null) {
      return null;
    }

    return CustomerDetailsResponse.AccountCredentialInfo.builder()
        .id(credential.getId())
        .emailAddress(credential.getEmailAddress())
        .phoneNumber(credential.getPhoneNumber())
        .status(credential.getStatus())
        .isEmailVerified(credential.isEmailVerified())
        .isPhoneVerified(credential.isPhoneVerified())
        .emailVerifiedAt(credential.getEmailVerifiedAt())
        .phoneVerifiedAt(credential.getPhoneVerifiedAt())
        .failedSignInAttempts(credential.getFailedSignInAttempts())
        .lockedUntil(credential.getLockedUntil())
        .lastSignedInAt(credential.getLastSignedInAt())
        .lastFailedSignIn(credential.getLastFailedSignInAt())
        .lastAccessChannel(credential.getLastAccessChannel())
        .lastPasswordChangeAt(credential.getLastPasswordChangeAt())
        .isPasswordResetTokenExpired(credential.isPasswordResetTokenExpired(LocalDateTime.now()))
        .requiresPasswordChange(credential.isRequiresPasswordChange())
        .mfaEnabled(credential.isMfaEnabled())
        .identityProvider(credential.getIdentityProvider())
        .deviceId(credential.getLastDeviceId())
        .notificationToken(null) // Field not available in Credential entity
        .roles(buildRoleInfo(credential.getRoles()))
        .build();
  }

  /**
   * Builds list of RoleInfo from Set of RoleDefinition.
   */
  private static List<CustomerDetailsResponse.RoleInfo> buildRoleInfo(
      java.util.Set<RoleDefinition> roles) {
    if (roles == null || roles.isEmpty()) {
      return List.of();
    }
    return roles.stream()
        .map(role -> CustomerDetailsResponse.RoleInfo.builder()
            .id(role.getId())
            .code(role.getCode())
            .name(role.getName())
            .description(role.getDescription())
            .active(role.isActive())
            .build())
        .collect(Collectors.toList());
  }

  /**
   * Builds AuditInfo from Customer entity.
   */
  private static CustomerDetailsResponse.AuditInfo buildAuditInfo(Customer customer) {
    var credential = customer.getCredential();
    return CustomerDetailsResponse.AuditInfo.builder()
        .customerCreatedAt(customer.getCreatedAt())
        .customerUpdatedAt(customer.getUpdatedAt())
        .customerCreatedBy(customer.getCreatedBy())
        .customerUpdatedBy(customer.getUpdatedBy())
        .customerCreatedIp(customer.getCreatedIp())
        .customerUpdatedIp(customer.getUpdatedIp())
        .customerVersion(customer.getVersion())
        .customerIsDeleted(customer.getIsDeleted())
        .customerDeletedAt(customer.getDeletedAt())
        .customerDeletedBy(customer.getDeletedBy())
        .customerRemarks(customer.getRemarks())
        .credentialCreatedAt(credential != null ? credential.getCreatedAt() : null)
        .credentialUpdatedAt(credential != null ? credential.getUpdatedAt() : null)
        .credentialCreatedBy(credential != null ? credential.getCreatedBy() : null)
        .credentialUpdatedBy(credential != null ? credential.getUpdatedBy() : null)
        .credentialVersion(credential != null ? credential.getVersion() : null)
        .credentialIsDeleted(credential != null ? credential.isDeleted() : null)
        .build();
  }

  /**
   * Builds full name from first name and last name.
   */
  private static String buildFullName(String firstName, String lastName) {
    if (firstName == null && lastName == null) {
      return null;
    }
    if (firstName == null) {
      return lastName;
    }
    if (lastName == null) {
      return firstName;
    }
    return firstName + " " + lastName;
  }

}

