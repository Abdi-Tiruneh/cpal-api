package com.commercepal.apiservice.users.migration;//package com.commercepal.apiservice.users.migration;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@Slf4j
//@RestController
//@RequestMapping("/api/v1/admin/migrations")
//@RequiredArgsConstructor
//public class MigrationController {
//
//  private final LoginToCredentialMigrationService migrationService;
//  private final OldCustomerToCustomerMigrationService oldCustomerToCustomerMigrationService;
//
//  @PostMapping("/login-to-credential")
//  public ResponseEntity<OldCustomerToCustomerMigrationService.MigrationResult> migrateLoginToCredential() {
//    log.info("Migration endpoint triggered");
//
////    LoginToCredentialMigrationService.MigrationResult result = migrationService.migrate();
//    OldCustomerToCustomerMigrationService.MigrationResult result = oldCustomerToCustomerMigrationService.migrate();
//
////    log.info("Migration completed successfully: {} created, {} skipped",
////        result.getCreated(), result.getSkipped());
//    return ResponseEntity.status(HttpStatus.OK).body(result);
//  }
//}
//
