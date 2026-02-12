package com.commercepal.apiservice.users.migration;//package com.commercepal.apiservice.users.migration;
//
//import com.commercepal.apiservice.shared.enums.Channel;
//import com.commercepal.apiservice.shared.enums.SupportedCurrency;
//import com.commercepal.apiservice.users.account_credential.Credential;
//import com.commercepal.apiservice.users.account_credential.CredentialRepository;
//import com.commercepal.apiservice.users.customer.Customer;
//import com.commercepal.apiservice.users.customer.CustomerRepository;
//import com.commercepal.apiservice.users.role.RoleCode;
//import com.commercepal.apiservice.users.role.RoleDefinition;
//import com.commercepal.apiservice.users.role.RoleDefinitionRepository;
//import java.time.LocalDateTime;
//import java.time.ZoneId;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Propagation;
//import org.springframework.transaction.annotation.Transactional;
//
///**
// * Professional migration service for migrating from OldCustomer to Customer. Manages Customer
// * relationship with Credential.
// */
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class OldCustomerToCustomerMigrationService {
//
//  private final OldCustomerRepository oldCustomerRepository;
//  private final CustomerRepository customerRepository;
//  private final CredentialRepository accountCredentialRepository;
//  private final LoginValidationRepository loginValidationRepository;
//  private final RoleDefinitionRepository roleDefinitionRepository;
//
//
//  public MigrationResult migrate() {
//    List<OldCustomer> oldCustomers = oldCustomerRepository.findAll();
////    List<OldCustomer> oldCustomers = oldCustomerRepository.findAll().subList(0, 10);
//    RoleDefinition roleDefinition = null;
//    try {
//      roleDefinition = roleDefinitionRepository.findByCode(RoleCode.ROLE_CUSTOMER).orElse(null);
//    } catch (Exception e) {
//      log.warn("⚠️ Could not fetch ROLE_CUSTOMER - role_definition table may not exist: {}",
//          e.getMessage());
//    }
//
//    if (roleDefinition == null) {
//      log.warn("⚠️ ROLE_CUSTOMER not found. Credentials will be created without role assignment.");
//      return null;
//    }
//
//    int created = 0;
//    int skipped = 0;
//    int failed = 0;
//    Map<Long, String> errors = new HashMap<>();
//    final int totalRecords = oldCustomers.size();
//    final int progressInterval = Math.max(100, totalRecords / 20);
//
//    log.info("🚀 Starting OldCustomer → Customer migration: {} records ()", totalRecords);
//
//    for (int i = 0; i < oldCustomers.size(); i++) {
//      OldCustomer oldCustomer = oldCustomers.get(i);
//
//      if ((i + 1) % progressInterval == 0 || i == 0) {
//        log.info("📊 Progress: {}/{} ({}%) - Created: {}, Skipped: {}, Failed: {}", i + 1,
//            totalRecords, ((i + 1) * 100 / totalRecords), created, skipped, failed);
//      }
//
//      try {
//        MigrationStatus status = migrateCustomerRecord(oldCustomer, roleDefinition);
//        switch (status) {
//          case CREATED -> created++;
//          case SKIPPED -> skipped++;
//          case FAILED -> {
//            failed++;
//            errors.put(oldCustomer.getCustomerId(), "Migration failed - check logs");
//          }
//        }
//      } catch (Exception ex) {
//        failed++;
//        String errorMsg = String.format("Unexpected error: %s", ex.getMessage());
//        errors.put(oldCustomer.getCustomerId(), errorMsg);
//        log.error("❌ Failed to migrate oldCustomer_id={} email={} phone={} → {}",
//            oldCustomer.getCustomerId(), oldCustomer.getEmailAddress(),
//            oldCustomer.getPhoneNumber(),
//            ex.getMessage());
//      }
//    }
//
//    return MigrationResult.builder().totalRecords(totalRecords).created(created).skipped(skipped)
//        .failed(failed).errors(errors).dryRun(false).build();
//  }
//
//  /**
//   * Migrate a single OldCustomer record to Customer. Each record is processed in its own
//   * transaction to prevent timeout issues.
//   *
//   * @param oldCustomer the OldCustomer record to migrate
//   * @return MigrationStatus indicating the result
//   */
//  @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 30)
//  public MigrationStatus migrateCustomerRecord(OldCustomer oldCustomer,
//      RoleDefinition roleDefinition) {
//    if (oldCustomer == null) {
//      log.warn("⚠️  Skipping null OldCustomer record");
//      return MigrationStatus.SKIPPED;
//    }
//
//    // Check if oldCustomer already exists for this credential
//    Optional<Customer> existingCustomer = customerRepository.findByOldCustomerId(
//        oldCustomer.getCustomerId());
//    if (existingCustomer.isPresent()) {
//      log.debug("⏭️  Skipping oldCustomer_id={} - Customer with credential_id={} already exists",
//          oldCustomer.getCustomerId(), oldCustomer.getCustomerId());
//      return MigrationStatus.SKIPPED;
//    }
//
//    // Check if oldCustomer already exists by account number
//    if (oldCustomer.getAccountNumber() != null && !oldCustomer.getAccountNumber().trim()
//        .isEmpty()) {
//      Optional<Customer> existingByAccountNumber = customerRepository.findByAccountNumber(
//          oldCustomer.getAccountNumber());
//      if (existingByAccountNumber.isPresent()) {
//        log.debug(
//            "⏭️  Skipping oldCustomer_id={} - Customer with account_number={} already exists",
//            oldCustomer.getCustomerId(), oldCustomer.getAccountNumber());
//        return MigrationStatus.SKIPPED;
//      }
//    }
//
//    // Check if oldCustomer already exists by commission account
//    if (oldCustomer.getCommissionAccount() != null && !oldCustomer.getCommissionAccount().trim()
//        .isEmpty()) {
//      Optional<Customer> existingByCommissionAccount = customerRepository.findByCommissionAccount(
//          oldCustomer.getCommissionAccount());
//      if (existingByCommissionAccount.isPresent()) {
//        log.debug(
//            "⏭️  Skipping oldCustomer_id={} - Customer with commission_account={} already exists",
//            oldCustomer.getCustomerId(), oldCustomer.getCommissionAccount());
//        return MigrationStatus.SKIPPED;
//      }
//    }
//
//    // Validate required fields
//    if (oldCustomer.getFirstName() == null || oldCustomer.getFirstName().trim().isEmpty()) {
//      log.warn("⚠️  Skipping oldCustomer_id={} - first name is null or empty",
//          oldCustomer.getCustomerId());
//      return MigrationStatus.SKIPPED;
//    }
//
//    if (oldCustomer.getAccountNumber() == null || oldCustomer.getAccountNumber().trim().isEmpty()) {
//      log.warn("⚠️  Skipping oldCustomer_id={} - account number is null or empty",
//          oldCustomer.getCustomerId());
//      return MigrationStatus.SKIPPED;
//    }
//
//    if (oldCustomer.getCommissionAccount() == null || oldCustomer.getCommissionAccount().trim()
//        .isEmpty()) {
//      log.warn("⚠️  Skipping oldCustomer_id={} - commission account is null or empty",
//          oldCustomer.getCustomerId());
//      return MigrationStatus.SKIPPED;
//    }
//
//    Credential accountCredential = getOrCreateAccountCredentials(oldCustomer,
//        roleDefinition);
//
//    try {
//      // Create Customer entity
//      Customer customer = new Customer();
//
//      // Map basic fields
//      customer.setFirstName(oldCustomer.getFirstName());
//      customer.setLastName(oldCustomer.getLastName());
//      customer.setAccountNumber(oldCustomer.getAccountNumber().trim());
//      customer.setCommissionAccount(oldCustomer.getCommissionAccount().trim());
//
//      // Map country
//      if (oldCustomer.getCountryIso() != null) {
//        customer.setCountry(oldCustomer.getCountryIso().getCode());
//      }
//
//      // Map city
//      if (oldCustomer.getCity() != null && !oldCustomer.getCity().trim().isEmpty()) {
//        customer.setCity(oldCustomer.getCity().trim());
//      }
//
//      customer.setPreferredLanguage("en"); // default
//
//      // Map preferred currency - default to ETB
//      customer.setPreferredCurrency(SupportedCurrency.ETB);
//
//      // Map referral code
//      if (oldCustomer.getReferralCode() != null && !oldCustomer.getReferralCode().trim()
//          .isEmpty()) {
//        customer.setReferralCode(oldCustomer.getReferralCode().trim());
//      }
//
//      // Map registration channel - default to WEB
//      customer.setRegistrationChannel(Channel.WEB);
//
//      // Set audit fields
//      customer.setCreatedBy(
//          oldCustomer.getRegisteredBy() != null ? oldCustomer.getRegisteredBy() : "migration");
//      customer.setIsDeleted(false);
//      customer.setVersion(0L);
//
//      // Set timestamps
//      if (oldCustomer.getCreatedAt() != null) {
//        customer.setCreatedAt(
//            LocalDateTime.ofInstant(oldCustomer.getCreatedAt().toInstant(),
//                ZoneId.systemDefault()));
//      } else if (oldCustomer.getRegisteredDate() != null) {
//        customer.setCreatedAt(LocalDateTime.ofInstant(oldCustomer.getRegisteredDate().toInstant(),
//            ZoneId.systemDefault()));
//      } else {
//        customer.setCreatedAt(LocalDateTime.now());
//      }
//
//      customer.setUpdatedAt(LocalDateTime.now());
//
//      // Link to Credential (OneToOne relationship)
//      customer.linkCredential(accountCredential);
//
////      customer.setDomainUserId(oldCustomer.getCustomerId());
//
//      // Save customer first to get the ID
//      customer = customerRepository.save(customer);
//
//      log.info("✅ Migrated oldCustomer_id={} → Customer id={} (account_number={})",
//          oldCustomer.getCustomerId(), customer.getId(), customer.getAccountNumber());
//
//      return MigrationStatus.CREATED;
//
//    } catch (Exception ex) {
//      log.error("❌ Failed to migrate oldCustomer_id={}: {}", oldCustomer.getCustomerId(),
//          ex.getMessage(), ex);
//      return MigrationStatus.FAILED;
//    }
//  }
//
//
//  @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 30)
//  public Credential getOrCreateAccountCredentials(OldCustomer oldCustomer,
//      RoleDefinition roleDefinition) {
//
//    Optional<Credential> accountCredential = accountCredentialRepository.findByEmailAddressOrPhoneNumber(
//        oldCustomer.getEmailAddress(), oldCustomer.getPhoneNumber());
//
//    if (accountCredential.isPresent()) {
//      return accountCredential.get();
//    }
//
//    //sure can get it
//    //sure can get it
//    LoginValidation login;
//
//    Optional<LoginValidation> loginValidation = loginValidationRepository.findLoginValidationByEmailAddress(
//        oldCustomer.getEmailAddress());
//
//    login = loginValidation.orElseGet(() -> loginValidationRepository.findByPhoneNumber(
//        oldCustomer.getPhoneNumber()).get(0));
//
//    if (login.getPasswordHash() == null || login.getPasswordHash().trim().isEmpty()) {
//      log.warn("⚠️  Skipping login_id={} - password hash is null or empty", login.getId());
//    }
//
//    try {
//      Credential credential = Credential.builder()
//          .emailAddress(login.getEmailAddress().trim()).phoneNumber(login.getPhoneNumber().trim())
//          .password(login.getPasswordHash()).status(login.getStatus())
//          .failedSignInAttempts(login.getLoginAttempts() != null ? login.getLoginAttempts() : 0)
//          .lockedUntil(login.getLoginLockedUntil() != null ? LocalDateTime.ofInstant(
//              login.getLoginLockedUntil(), ZoneId.systemDefault()) : null).lastSignedInAt(
//              login.getLastAttemptAt() != null ? LocalDateTime.ofInstant(login.getLastAttemptAt(),
//                  ZoneId.systemDefault()) : null).emailVerifiedAt(
//              login.getIsEmailVerified() != null && login.getIsEmailVerified() ? (
//                  login.getCreatedAt() != null ? LocalDateTime.ofInstant(login.getCreatedAt(),
//                      ZoneId.systemDefault()) : LocalDateTime.now()) : null).phoneVerifiedAt(
//              login.getIsPhoneVerified() != null && login.getIsPhoneVerified() ? (
//                  login.getCreatedAt() != null ? LocalDateTime.ofInstant(login.getCreatedAt(),
//                      ZoneId.systemDefault()) : LocalDateTime.now()) : null)
//          .passwordResetToken(login.getResetToken())
////          .passwordResetExpiresAt(login.getResetTokenExpiry() != null
////              ? LocalDateTime.ofInstant(login.getResetTokenExpiry(), ZoneId.systemDefault())
////              : null)
//          .identityProviderUserId(login.getOauthProviderUserId()).deviceId(login.getDeviceId())
//          .isPhoneVerified(login.getIsPhoneVerified() != null ? login.getIsPhoneVerified() : false)
//          .isEmailVerified(login.getIsEmailVerified() != null ? login.getIsEmailVerified() : false)
//          .notificationToken(login.getNotificationToken()).createdBy("anonymous").isDeleted(false)
//          .version(0L).build();
//
//      if (roleDefinition != null) {
//        credential.assignRole(roleDefinition);
//      }
//
//      if (login.getCreatedAt() != null) {
//        credential.setCreatedAt(
//            LocalDateTime.ofInstant(login.getCreatedAt(), ZoneId.systemDefault()));
//      }
//
//      credential.setUpdatedAt(LocalDateTime.now());
//      return accountCredentialRepository.save(credential);
//    } catch (Exception ex) {
//      log.error("❌ Failed to migrate login_id={}: {}", login.getId(), ex.getMessage(), ex);
//      return null;
//    }
//  }
//
//
//  /**
//   * Migration status enum.
//   */
//  public enum MigrationStatus {
//    CREATED, SKIPPED, FAILED
//  }
//
//  /**
//   * Migration result DTO.
//   */
//  @lombok.Builder
//  @lombok.Getter
//  public static class MigrationResult {
//
//    private final int totalRecords;
//    private final int created;
//    private final int skipped;
//    private final int failed;
//    private final Map<Long, String> errors;
//    private final boolean dryRun;
//  }
//}
