//package com.commercepal.apiservice.users.migration;
//
//import com.commercepal.apiservice.users.migration.LoginToCredentialMigrationService.MigrationResult;
//import java.time.LocalDateTime;
//import java.util.Map;
//
//public record MigrationResponse(
//    int totalRecords,
//    int created,
//    int skipped,
//    int failed,
//    Map<Long, String> errors,
//    LocalDateTime completedAt,
//    String status
//) {
//
//  public static MigrationResponse from(MigrationResult result) {
//    String status = result.getFailed() > 0 ? "COMPLETED_WITH_ERRORS"
//        : result.getCreated() > 0 ? "COMPLETED_SUCCESSFULLY"
//            : "NO_RECORDS_PROCESSED";
//
//    return new MigrationResponse(
//        result.getTotalRecords(),
//        result.getCreated(),
//        result.getSkipped(),
//        result.getFailed(),
//        result.getErrors(),
//        LocalDateTime.now(),
//        status
//    );
//  }
//}
//
