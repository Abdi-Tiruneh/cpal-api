package com.commercepal.apiservice.users.credential;

import com.commercepal.apiservice.users.credential.dto.AdminPasswordResetRequest;
import com.commercepal.apiservice.users.credential.dto.ForgotPasswordRequest;
import com.commercepal.apiservice.users.credential.dto.PasswordChangeRequest;
import com.commercepal.apiservice.users.credential.dto.PasswordResetRequest;
import com.commercepal.apiservice.users.credential.dto.StaffInitialPasswordSetRequest;

/**
 * Service interface for credential management in e-commerce platform.
 */
public interface CredentialService {
  /**
   * Change password for authenticated user.
   */
  void changePassword(Credential credential, PasswordChangeRequest requestDto);

  /**
   * Reset password using 6-digit verification token.
   */
  void resetPasswordWithToken(PasswordResetRequest requestDto);

  /**
   * Initiate forgot password flow by generating and sending 6-digit verification code.
   */
  void initiateForgotPassword(ForgotPasswordRequest requestDto);

  /**
   * Set initial password for the current staff user. Only staff users can use this.
   */
  void setStaffInitialPassword(Credential currentUser, StaffInitialPasswordSetRequest requestDto);

  /**
   * Admin operation to reset user password.
   */
  void adminResetPassword(Credential admin, AdminPasswordResetRequest requestDto);

  boolean existsByEmail(String email);

  boolean existsByPhone(String phoneNumber);
}
