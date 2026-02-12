package com.commercepal.apiservice.users.customer;

import com.commercepal.apiservice.users.customer.dto.CustomerDetailResponse;
import com.commercepal.apiservice.users.customer.dto.CustomerDetailsResponse;
import com.commercepal.apiservice.users.customer.dto.CustomerDetailsResponse.AccountCredentialInfo;
import com.commercepal.apiservice.users.customer.dto.CustomerDetailsResponse.AuditInfo;
import com.commercepal.apiservice.users.customer.dto.CustomerDetailsResponse.CustomerInfo;
import com.commercepal.apiservice.users.customer.dto.CustomerDetailsResponse.RoleInfo;
import com.commercepal.apiservice.users.customer.dto.CustomerPageRequestDto;
import com.commercepal.apiservice.users.role.RoleDefinition;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Admin service for customer management operations.
 * <p>
 * Provides comprehensive admin functionality for viewing and managing customers, including advanced
 * filtering, pagination, and detailed customer information.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class CustomerAdminService {

  private final CustomerRepository customerRepository;

  /**
   * Get paginated list of customers with advanced filtering and sorting.
   * <p>
   * Supports multi-level sorting, keyword search, and comprehensive filtering across Customer and
   * Credential entities.
   *
   * @param requestDto the page request containing pagination, sorting, and filter criteria
   * @return paginated list of customer detail responses
   */
  public Page<CustomerDetailResponse> getCustomers(CustomerPageRequestDto requestDto) {
    log.debug("[CUSTOMER-ADMIN] Fetching customers with filters: hasFilters={}, page={}, size={}",
        requestDto.hasFilters(), requestDto.page(), requestDto.size());

    Specification<Customer> specification = CustomerSpecification.buildSpecification(requestDto);
    Pageable pageable = requestDto.toPageable();

    Page<Customer> customersPage = customerRepository.findAll(specification, pageable);

    log.info("[CUSTOMER-ADMIN] Found {} customers (page {} of {})",
        customersPage.getTotalElements(),
        customersPage.getNumber() + 1,
        customersPage.getTotalPages());

    return customersPage.map(this::toDetailResponse);
  }

  /**
   * Get detailed customer information by ID.
   * <p>
   * Returns comprehensive hierarchical customer data including profile, account credentials, and
   * audit information.
   *
   * @param customerId the customer ID
   * @return optional containing customer details if found
   */
  public Optional<CustomerDetailsResponse> getCustomerById(Long customerId) {
    log.debug("[CUSTOMER-ADMIN] Fetching customer details for ID: {}", customerId);

    Optional<Customer> customerOpt = customerRepository.findById(customerId);

    if (customerOpt.isEmpty()) {
      log.warn("[CUSTOMER-ADMIN] Customer not found with ID: {}", customerId);
      return Optional.empty();
    }

    Customer customer = customerOpt.get();
    CustomerDetailsResponse response = toDetailsResponse(customer);

    log.info("[CUSTOMER-ADMIN] Retrieved customer details for ID: {}, accountNumber: {}",
        customerId, customer.getAccountNumber());

    return Optional.of(response);
  }

  /**
   * Get customer by account number.
   *
   * @param accountNumber the account number
   * @return optional containing customer details if found
   */
  public Optional<CustomerDetailsResponse> getCustomerByAccountNumber(String accountNumber) {
    log.debug("[CUSTOMER-ADMIN] Fetching customer by account number: {}", accountNumber);

    return customerRepository.findByAccountNumber(accountNumber)
        .map(this::toDetailsResponse);
  }

  /**
   * Get customer by referral code.
   *
   * @param referralCode the referral code
   * @return optional containing customer details if found
   */
  public Optional<CustomerDetailsResponse> getCustomerByReferralCode(String referralCode) {
    log.debug("[CUSTOMER-ADMIN] Fetching customer by referral code: {}", referralCode);

    return customerRepository.findByReferralCode(referralCode)
        .map(this::toDetailsResponse);
  }

  /**
   * Check if customer exists by ID.
   *
   * @param customerId the customer ID
   * @return true if customer exists
   */
  public boolean customerExists(Long customerId) {
    return customerRepository.existsById(customerId);
  }

  /**
   * Get total count of customers matching the filter criteria.
   *
   * @param requestDto the filter criteria
   * @return count of matching customers
   */
  public long getCustomerCount(CustomerPageRequestDto requestDto) {
    Specification<Customer> specification = CustomerSpecification.buildSpecification(requestDto);
    return customerRepository.count(specification);
  }

  // ========== MAPPING METHODS ==========

  /**
   * Maps Customer entity to CustomerDetailResponse (simple table view).
   */
  private CustomerDetailResponse toDetailResponse(Customer customer) {
    if (customer == null) {
      return null;
    }

    var credential = customer.getCredential();
    String fullName = buildFullName(customer.getFirstName(), customer.getLastName());

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
   * Maps Customer entity to CustomerDetailsResponse (hierarchical detailed view).
   */
  private CustomerDetailsResponse toDetailsResponse(Customer customer) {
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
  private CustomerInfo buildCustomerInfo(Customer customer) {
    return CustomerInfo.builder()
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
  private AccountCredentialInfo buildAccountCredentialInfo(Customer customer) {
    var credential = customer.getCredential();
    if (credential == null) {
      return null;
    }

    return AccountCredentialInfo.builder()
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
//        .notificationToken(credential.getNotificationToken())
        .roles(buildRoleInfo(credential.getRoles()))
        .build();
  }

  /**
   * Builds list of RoleInfo from Set of RoleDefinition.
   */
  private List<RoleInfo> buildRoleInfo(Set<RoleDefinition> roles) {
    if (roles == null || roles.isEmpty()) {
      return List.of();
    }
    return roles.stream()
        .map(role -> RoleInfo.builder()
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
  private AuditInfo buildAuditInfo(Customer customer) {
    var credential = customer.getCredential();
    return AuditInfo.builder()
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
  private String buildFullName(String firstName, String lastName) {
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

