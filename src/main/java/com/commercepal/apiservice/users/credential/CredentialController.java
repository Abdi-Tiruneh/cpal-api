package com.commercepal.apiservice.users.credential;

import com.commercepal.apiservice.users.credential.dto.ForgotPasswordRequest;
import com.commercepal.apiservice.users.credential.dto.PasswordChangeRequest;
import com.commercepal.apiservice.users.credential.dto.PasswordResetRequest;
import com.commercepal.apiservice.utils.ClientIpUtils;
import com.commercepal.apiservice.utils.CurrentUserService;
import com.commercepal.apiservice.utils.response.ResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for user-facing credential management operations. Handles password change and
 * reset for e-commerce users.
 */
@RestController
@RequestMapping("/api/v1/credentials")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Credential Management (User)", description = "APIs for user-facing credential operations (password change, reset)")
public class CredentialController {

  private final CredentialService credentialService;
  private final CurrentUserService currentUserService;

  @PutMapping("/password/change")
  @SecurityRequirement(name = "Bearer Authentication")
  @Operation(summary = "Change password", description =
      "Authenticated user can change their password. "
          + "Requires current password for verification.")
  public ResponseEntity<ResponseWrapper<Object>> changePassword(
      @Valid @RequestBody PasswordChangeRequest requestDto, HttpServletRequest request) {

    Credential currentUser = currentUserService.getCurrentUser();
    log.info("[CREDENTIAL] User {} requesting password change", currentUser.getUsername());

    String ipAddress = ClientIpUtils.getClientIpAddress(request);
    credentialService.changePassword(currentUser, requestDto);

    log.info("[CREDENTIAL] Password changed successfully for user: {}",
        currentUser.getUsername());

    return ResponseWrapper.success("Password changed successfully!");
  }

  @PostMapping("/password/forgot")
  @Operation(summary = "Initiate forgot password flow", description =
      "Initiates password reset flow by sending 6-digit verification code to user's email or phone number.")
  public ResponseEntity<ResponseWrapper<Object>> forgotPassword(
      @Valid @RequestBody ForgotPasswordRequest requestDto, HttpServletRequest request) {

    log.info("[CREDENTIAL] Initiating forgot password for: {}", requestDto.emailOrPhone());

    String ipAddress = ClientIpUtils.getClientIpAddress(request);
    credentialService.initiateForgotPassword(requestDto);

    log.info("[CREDENTIAL] Forgot password initiated for: {}", requestDto.emailOrPhone());

    return ResponseWrapper.success(
        "If an account exists with this email or phone, a verification code has been sent.");
  }

  @PostMapping("/password/reset")
  @Operation(summary = "Reset password with token", description =
      "Reset password using 6-digit verification code received via email or SMS. "
          + "Part of forgot password flow.")
  public ResponseEntity<ResponseWrapper<Object>> resetPassword(
      @Valid @RequestBody PasswordResetRequest requestDto, HttpServletRequest request) {

    log.info("[CREDENTIAL] Received password reset request with token");

    String ipAddress = ClientIpUtils.getClientIpAddress(request);
    credentialService.resetPasswordWithToken(requestDto);

    log.info("[CREDENTIAL] Password reset successfully");

    return ResponseWrapper.success(
        "Password reset successfully! You can now login with your new password.");
  }

}

