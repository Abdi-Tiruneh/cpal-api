package com.commercepal.apiservice.users.credential;

import com.commercepal.apiservice.shared.exceptions.business.BadRequestException;
import com.commercepal.apiservice.shared.exceptions.resource.ResourceNotFoundException;
import com.commercepal.apiservice.users.credential.dto.AdminPasswordResetRequest;
import com.commercepal.apiservice.users.credential.dto.ForgotPasswordRequest;
import com.commercepal.apiservice.users.credential.dto.PasswordChangeRequest;
import com.commercepal.apiservice.users.credential.dto.PasswordResetRequest;
import com.commercepal.apiservice.users.credential.dto.StaffInitialPasswordSetRequest;
import com.commercepal.apiservice.users.enums.UserType;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link CredentialService} for credential management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CredentialServiceImpl implements CredentialService {

  private static final int TOKEN_EXPIRY_MINUTES = 15;
  private static final SecureRandom random = new SecureRandom();

  private final CredentialRepository credentialRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void changePassword(Credential credential, PasswordChangeRequest requestDto) {
    log.info("[CREDENTIAL] Changing password | username={}", credential.getUsername());

    if (!requestDto.newPassword().equals(requestDto.confirmPassword())) {
      throw new BadRequestException("New password and confirmation do not match");
    }

    if (!passwordEncoder.matches(requestDto.currentPassword(), credential.getPassword())) {
      throw new BadRequestException("Current password is incorrect");
    }

    if (passwordEncoder.matches(requestDto.newPassword(), credential.getPassword())) {
      throw new BadRequestException("New password cannot be the same as current password");
    }

    String encodedPassword = passwordEncoder.encode(requestDto.newPassword());
    credential.setPasswordHash(encodedPassword);
    credential.setLastPasswordChangeAt(LocalDateTime.now());
    credential.setRequiresPasswordChange(false);

    credentialRepository.save(credential);

    log.info("[CREDENTIAL] Password changed successfully | username={}", credential.getUsername());
  }

  @Override
  public void resetPasswordWithToken(PasswordResetRequest requestDto) {
    log.info("[CREDENTIAL] Resetting password with token | target={}", requestDto.target());

    if (!requestDto.newPassword().equals(requestDto.confirmPassword())) {
      throw new BadRequestException("Passwords do not match");
    }

    Credential credential = credentialRepository
        .findByIdentifier(requestDto.target())
        .orElseThrow(() -> new ResourceNotFoundException(
            "Account not found with email or phone: " + requestDto.target()));

    if (credential.isPasswordResetTokenInvalidated()) {
      log.warn("[CREDENTIAL] Password reset token invalidated due to 3 failed attempts | username={}",
          credential.getUsername());
      credential.clearPasswordResetToken();
      credentialRepository.save(credential);
      throw new BadRequestException(
          "Verification token has been invalidated due to too many failed attempts. Please request a new verification code.");
    }

    if (credential.getPasswordResetToken() == null) {
      throw new BadRequestException(
          "No active verification token found. Please request a new verification code.");
    }

    if (credential.isPasswordResetTokenExpired(LocalDateTime.now())) {
      throw new BadRequestException(
          "Verification token has expired. Please request a new verification code.");
    }

    if (!credential.getPasswordResetToken().equals(requestDto.verificationToken())) {
      log.warn("[CREDENTIAL] Invalid verification token attempt | username={} | attempt={}",
          credential.getUsername(), credential.getPasswordResetFailedAttempts() + 1);

      credential.incrementPasswordResetFailedAttempts();

      if (credential.isPasswordResetTokenInvalidated()) {
        log.warn("[CREDENTIAL] Password reset token invalidated after 3 failed attempts | username={}",
            credential.getUsername());
        credential.clearPasswordResetToken();
        credentialRepository.save(credential);
        throw new BadRequestException(
            "Invalid verification token. Maximum attempts exceeded. Please request a new verification code.");
      }

      credentialRepository.save(credential);
      throw new BadRequestException(
          String.format("Invalid verification token. %d attempt(s) remaining.",
              3 - credential.getPasswordResetFailedAttempts()));
    }

    if (passwordEncoder.matches(requestDto.newPassword(), credential.getPassword())) {
      throw new BadRequestException("New password cannot be the same as current password");
    }

    String encodedPassword = passwordEncoder.encode(requestDto.newPassword());
    credential.setPasswordHash(encodedPassword);
    credential.setLastPasswordChangeAt(LocalDateTime.now());
    credential.setRequiresPasswordChange(false);

    credential.clearPasswordResetToken();

    credentialRepository.save(credential);

    log.info("[CREDENTIAL] Password reset successfully | username={}", credential.getUsername());
  }

  @Override
  public void initiateForgotPassword(ForgotPasswordRequest requestDto) {
    log.info("[CREDENTIAL] Initiating forgot password flow | emailOrPhone={}", requestDto.emailOrPhone());

    Credential credential = credentialRepository
        .findByIdentifier(requestDto.emailOrPhone())
        .orElseThrow(() -> new ResourceNotFoundException(
            "Account not found with email or phone: " + requestDto.emailOrPhone()));

    String verificationCode = generateSixDigitToken();
    LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES);

    credential.markPasswordResetToken(verificationCode, expiresAt);
    credentialRepository.save(credential);

    log.info("[CREDENTIAL] Password reset token generated | username={} | expiresAt={}",
        credential.getUsername(), expiresAt);
  }

  @Override
  public void setStaffInitialPassword(Credential currentUser, StaffInitialPasswordSetRequest requestDto) {
    if (currentUser.getUserType() != UserType.STAFF) {
      throw new BadRequestException("Only staff users can set initial password via this endpoint");
    }

    log.info("[CREDENTIAL] Staff setting initial password | username={}", currentUser.getUsername());

    if (!requestDto.newPassword().equals(requestDto.confirmPassword())) {
      throw new BadRequestException("New password and confirmation do not match");
    }

    String encodedPassword = passwordEncoder.encode(requestDto.newPassword());
    currentUser.setPasswordHash(encodedPassword);
    currentUser.setLastPasswordChangeAt(LocalDateTime.now());
    currentUser.setRequiresPasswordChange(false);

    credentialRepository.save(currentUser);

    log.info("[CREDENTIAL] Staff initial password set | username={}", currentUser.getUsername());
  }

  @Override
  public void adminResetPassword(Credential admin, AdminPasswordResetRequest requestDto) {
    log.info("[CREDENTIAL] Admin resetting password | adminUsername={} | targetUserId={}",
        admin.getUsername(), requestDto.userId());

    Credential targetUser = credentialRepository.findById(requestDto.userId())
        .orElseThrow(
            () -> new ResourceNotFoundException("User not found with ID: " + requestDto.userId()));

    if (passwordEncoder.matches(requestDto.newPassword(), targetUser.getPassword())) {
      throw new BadRequestException("New password cannot be the same as current password");
    }

    String encodedPassword = passwordEncoder.encode(requestDto.newPassword());
    targetUser.setPasswordHash(encodedPassword);
    targetUser.setLastPasswordChangeAt(LocalDateTime.now());
    targetUser.setRequiresPasswordChange(
        requestDto.requirePasswordChange() != null && requestDto.requirePasswordChange());

    credentialRepository.save(targetUser);

    log.info("[CREDENTIAL] Admin reset password completed | adminUsername={} | targetUsername={}",
        admin.getUsername(), targetUser.getUsername());
  }

  private String generateSixDigitToken() {
//    int token = 100000 + random.nextInt(900000); // Generates number between 100000 and 999999
//    return String.valueOf(token);
//
    return "123456";
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsByEmail(String email) {
    return credentialRepository.existsByEmailAddressAndDeletedFalse(email);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsByPhone(String phoneNumber) {
    return credentialRepository.existsByPhoneNumberAndDeletedFalse(phoneNumber);
  }
}
