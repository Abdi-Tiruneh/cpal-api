//package com.commercepal.apiservice.users.migration;
//
//import com.commercepal.apiservice.users.credential.Credential;
//import com.commercepal.apiservice.users.credential.CredentialRepository;
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
// * Professional migration service for migrating from LoginValidation to Credential.
// */
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class LoginToCredentialMigrationService {
//
//  private final LoginValidationRepository loginValidationRepository;
//  private final CredentialRepository accountCredentialRepository;
//  private final RoleDefinitionRepository roleDefinitionRepository;
//
//  /**
//   * Migrate all LoginValidation records to Credential with optional dry-run mode. Each
//   * record is processed in its own transaction to prevent timeout issues.
//   *
//   * @return MigrationResult containing statistics and any errors
//   */
//  public MigrationResult migrate() {
////    List<LoginValidation> logins = loginValidationRepository.findAll();
//    List<LoginValidation> logins = loginValidationRepository.findAll().subList(1, 10);
//
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
//    }
//
//    int created = 0;
//    int skipped = 0;
//    int failed = 0;
//    Map<Long, String> errors = new HashMap<>();
//    final int totalRecords = logins.size();
//    final int progressInterval = Math.max(100, totalRecords / 20);
//
//    log.info("🚀 Starting LoginValidation → Credential migration: {} records ",
//        totalRecords);
//
//    for (int i = 0; i < logins.size(); i++) {
//      LoginValidation login = logins.get(i);
//
//      if ((i + 1) % progressInterval == 0 || i == 0) {
//        log.info("📊 Progress: {}/{} ({}%) - Created: {}, Skipped: {}, Failed: {}",
//            i + 1, totalRecords, ((i + 1) * 100 / totalRecords), created, skipped, failed);
//      }
//
//      try {
//        MigrationStatus status = migrateLoginRecord(login, roleDefinition);
//        switch (status) {
//          case CREATED -> created++;
//          case SKIPPED -> skipped++;
//          case FAILED -> {
//            failed++;
//            errors.put(login.getId(), "Migration failed - check logs");
//          }
//        }
//      } catch (Exception ex) {
//        failed++;
//        String errorMsg = String.format("Unexpected error: %s", ex.getMessage());
//        errors.put(login.getId(), errorMsg);
//        log.error("❌ Failed to migrate login_id={} email={} phone={} → {}", login.getId(),
//            login.getEmailAddress(), login.getPhoneNumber(), ex.getMessage());
//      }
//    }
//
//    MigrationResult result = MigrationResult.builder()
//        .totalRecords(totalRecords)
//        .created(created)
//        .skipped(skipped)
//        .failed(failed)
//        .errors(errors)
//        .build();
//
//    log.info(
//        "✅ Migration completed: {} created, {} skipped, {} failed out of {} total ",
//        created, skipped, failed, totalRecords);
//
//    return result;
//  }
//
//
//  /**
//   * Migrate a single LoginValidation record to Credential. Each record is processed in its
//   * own transaction to prevent timeout issues.
//   *
//   * @param login          the LoginValidation record to migrate
//   * @param roleDefinition the default role to assign
//   * @return MigrationStatus indicating the result
//   */
//  @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 30)
//  public MigrationStatus migrateLoginRecord(LoginValidation login,
//      RoleDefinition roleDefinition) {
//    if (login == null) {
//      log.warn("⚠️  Skipping null LoginValidation record");
//      return MigrationStatus.SKIPPED;
//    }
//
//    Optional<Credential> existing = Optional.empty();
//
//    if (login.getEmailAddress() != null && !login.getEmailAddress().trim().isEmpty()) {
//      existing = accountCredentialRepository.findByEmailAddress(login.getEmailAddress());
//    }
//
//    if (existing.isEmpty() && login.getPhoneNumber() != null && !login.getPhoneNumber().trim()
//        .isEmpty()) {
//      existing = accountCredentialRepository.findByPhoneNumber(login.getPhoneNumber());
//    }
//
//    if (existing.isPresent()) {
//      return MigrationStatus.SKIPPED;
//    }
//
//    if (login.getEmailAddress() == null && login.getPhoneNumber() == null) {
//      log.warn("⚠️  Skipping login_id={} - both email and phone are null", login.getId());
//      return MigrationStatus.SKIPPED;
//    }
//
//    if (login.getPasswordHash() == null || login.getPasswordHash().trim().isEmpty()) {
//      log.warn("⚠️  Skipping login_id={} - password hash is null or empty", login.getId());
//      return MigrationStatus.SKIPPED;
//    }
//
//    try {
//
//      Credential credential = Credential.builder()
//          .emailAddress(login.getEmailAddress().trim())
//          .phoneNumber(login.getPhoneNumber().trim())
//          .passwordHash(login.getPasswordHash())
//          .status(login.getStatus())
//          .failedSignInAttempts(login.getLoginAttempts() != null ? login.getLoginAttempts() : 0)
//          .lockedUntil(login.getLoginLockedUntil() != null
//              ? LocalDateTime.ofInstant(login.getLoginLockedUntil(), ZoneId.systemDefault())
//              : null)
//          .lastSignedInAt(login.getLastAttemptAt() != null
//              ? LocalDateTime.ofInstant(login.getLastAttemptAt(), ZoneId.systemDefault())
//              : null)
//          .emailVerifiedAt(login.getIsEmailVerified() != null && login.getIsEmailVerified()
//              ? (login.getCreatedAt() != null
//              ? LocalDateTime.ofInstant(login.getCreatedAt(), ZoneId.systemDefault())
//              : LocalDateTime.now())
//              : null)
//          .phoneVerifiedAt(login.getIsPhoneVerified() != null && login.getIsPhoneVerified()
//              ? (login.getCreatedAt() != null
//              ? LocalDateTime.ofInstant(login.getCreatedAt(), ZoneId.systemDefault())
//              : LocalDateTime.now())
//              : null)
//          .passwordResetToken(login.getResetToken())
////          .passwordResetExpiresAt(login.getResetTokenExpiry() != null
////              ? LocalDateTime.ofInstant(login.getResetTokenExpiry(), ZoneId.systemDefault())
////              : null)
//          .identityProviderUserId(login.getOauthProviderUserId())
//          .deviceId(login.getDeviceId())
//          .isPhoneVerified(login.getIsPhoneVerified() != null ? login.getIsPhoneVerified() : false)
//          .isEmailVerified(login.getIsEmailVerified() != null ? login.getIsEmailVerified() : false)
//          .notificationToken(login.getNotificationToken())
//          .createdBy("anonymous")
//          .isDeleted(false)
//          .version(0L)
//          .build();
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
//      accountCredentialRepository.save(credential);
//
//      return MigrationStatus.CREATED;
//
//    } catch (Exception ex) {
//      log.error("❌ Failed to migrate login_id={}: {}", login.getId(), ex.getMessage(), ex);
//      return MigrationStatus.FAILED;
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