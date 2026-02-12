package com.commercepal.apiservice.users.credential;

import com.commercepal.apiservice.users.credential.dto.AdminPasswordResetRequest;
import com.commercepal.apiservice.users.credential.dto.StaffInitialPasswordSetRequest;
import com.commercepal.apiservice.utils.CurrentUserService;
import com.commercepal.apiservice.utils.response.ResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/admin/credentials")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Credential Management (Admin)", description = "Administrative APIs for managing user credentials")
public class AdminCredentialController {

  private final CredentialService credentialService;
  private final CurrentUserService currentUserService;

  @PostMapping("/staff/password/set-initial")
  @Operation(
      summary = "Set staff initial password",
      description = "Authenticated staff member sets their own initial password. "
          + "Only staff user type is allowed. Enforces strong password: min 12 chars, uppercase, lowercase, digit, special character."
  )
  public ResponseEntity<ResponseWrapper<String>> setStaffInitialPassword(
      @Valid @RequestBody StaffInitialPasswordSetRequest requestDto) {

    Credential currentUser = currentUserService.getCurrentUser();
    credentialService.setStaffInitialPassword(currentUser, requestDto);

    return ResponseWrapper.success("Staff initial password set successfully");
  }

  @PostMapping("/users/password/reset")
  @Operation(
      summary = "Admin reset user password",
      description = "Administrators can reset password for any user. "
          + "Can optionally force password change on next login."
  )
  public ResponseEntity<ResponseWrapper<String>> adminResetPassword(
      @Valid @RequestBody AdminPasswordResetRequest requestDto) {

    Credential admin = currentUserService.getCurrentUser();
    log.info("[ADMIN-CREDENTIAL] Admin {} requesting password reset for user ID: {}",
        admin.getUsername(), requestDto.userId());

    credentialService.adminResetPassword(admin, requestDto);

    log.info("[ADMIN-CREDENTIAL] Admin {} reset password for user ID: {}",
        admin.getUsername(), requestDto.userId());

    return ResponseWrapper.success("User password reset successfully");
  }
}

