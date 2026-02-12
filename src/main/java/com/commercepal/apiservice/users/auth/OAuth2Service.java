package com.commercepal.apiservice.users.auth;

import com.commercepal.apiservice.shared.enums.SupportedCountry;
import com.commercepal.apiservice.shared.enums.SupportedCurrency;
import com.commercepal.apiservice.shared.exceptions.business.BadRequestException;
import com.commercepal.apiservice.shared.security.CustomUserDetailsService;
import com.commercepal.apiservice.shared.security.JwtTokenProvider;
import com.commercepal.apiservice.users.credential.Credential;
import com.commercepal.apiservice.users.enums.UserType;
import com.commercepal.apiservice.users.credential.CredentialRepository;
import com.commercepal.apiservice.users.auth.dto.OAuth2CompleteProfileRequest;
import com.commercepal.apiservice.users.auth.dto.OAuth2LoginRequest;
import com.commercepal.apiservice.users.auth.dto.OAuth2LoginResponse;
import com.commercepal.apiservice.users.auth.dto.OAuth2SetPasswordRequest;
import com.commercepal.apiservice.users.customer.Customer;
import com.commercepal.apiservice.users.customer.CustomerRepository;
import com.commercepal.apiservice.users.enums.IdentityProvider;
import com.commercepal.apiservice.users.enums.UserStatus;
import com.commercepal.apiservice.users.referral.ReferralCodeUtils;
import com.commercepal.apiservice.users.role.RoleCode;
import com.commercepal.apiservice.users.role.RoleDefinition;
import com.commercepal.apiservice.users.role.RoleDefinitionRepository;
import com.commercepal.apiservice.users.till.TillSequenceService;
import com.commercepal.apiservice.utils.ClientIpUtils;
import com.commercepal.apiservice.utils.DeviceFingerprintUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for handling OAuth2 authentication operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OAuth2Service {

  private final CredentialRepository credentialRepository;
  private final CustomerRepository customerRepository;
  private final JwtTokenProvider jwtTokenProvider;
  private final CustomUserDetailsService userDetailsService;
  private final TillSequenceService tillSequenceService;
  private final ReferralCodeUtils referralCodeUtils;
  private final RoleDefinitionRepository roleDefinitionRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * Process OAuth2 login request. Creates a new user if not exists, otherwise logs in.
   */
  public OAuth2LoginResponse processOAuth2Login(OAuth2LoginRequest request,
      HttpServletRequest httpRequest) {
    log.info("[OAuth2] Processing login for provider: {}, user: {}", request.provider(),
        request.providerUserId());

    // 1. Check if user already exists by provider + providerUserId
    Optional<Credential> existingCredential = credentialRepository
        .findByIdentityProviderAndIdentityProviderUserId(request.provider(),
            request.providerUserId());

    Credential credential;
    boolean isNewUser = false;

    if (existingCredential.isPresent()) {
      credential = existingCredential.get();
      log.info("[OAuth2] Existing user found | username={}", credential.getUsername());
      updateExistingUser(credential, request);
    } else {
      if (request.email() != null && !request.email().isBlank()) {
        Optional<Credential> emailCredential = credentialRepository.findByEmailAddress(
            request.email());

        if (emailCredential.isPresent()) {
          credential = emailCredential.get();
          log.info("[OAuth2] Linking existing email account | email={}", request.email());
          linkAccount(credential, request);
        } else {
          log.info("[OAuth2] Creating new user | provider={}", request.provider());
          credential = createNewOAuth2User(request);
          isNewUser = true;
        }
      } else {
        log.info("[OAuth2] Creating new user without email | provider={}", request.provider());
        credential = createNewOAuth2User(request);
        isNewUser = true;
      }
    }

    // 3. Generate tokens
    String deviceFingerprint = DeviceFingerprintUtils.extractDeviceFingerprint(httpRequest);
    String ipAddress = ClientIpUtils.getClientIpAddress(httpRequest);

    // We use the credential ID or username as the principal subject
    String principal = credential.getUsername() != null ? credential.getUsername()
        : credential.getIdentityProviderUserId();

    // Load UserDetails to ensure roles/authorities are populated correctly
    // Note: If username is null (e.g. only provider ID), we might need special
    // handling in CustomUserDetailsService
    // For now, we assume we can load by whatever is set as username/email/phone
    UserDetails userDetails;
    try {
      if (credential.getEmailAddress() != null) {
        userDetails = userDetailsService.loadUserByUsername(credential.getEmailAddress());
      } else if (credential.getPhoneNumber() != null) {
        userDetails = userDetailsService.loadUserByUsername(credential.getPhoneNumber());
      } else {
        // Fallback for just created user with only provider ID - might need to support
        // finding by ID
        // Or we construct a minimal UserDetails object here
        userDetails = credential;
      }
    } catch (Exception e) {
      // Fallback
      userDetails = credential;
    }

    JwtTokenProvider.TokenPair tokenPair = jwtTokenProvider.generateTokenPair(
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()),
        deviceFingerprint,
        ipAddress,
        principal);

    // 4. Update last signed in
    credential.setLastSignedInAt(LocalDateTime.now());
    credential.setLastAccessChannel(request.channel());
    credentialRepository.save(credential);

    // 5. Check profile status
    ProfileCompletionStatus profileStatus = checkProfileStatus(credential);
    boolean requiresProfileCompletion = profileStatus != ProfileCompletionStatus.COMPLETE;

    return new OAuth2LoginResponse(
        tokenPair.getAccessToken(),
        tokenPair.getRefreshToken(),
        tokenPair.getTokenType(),
        tokenPair.getExpiresIn(),
        isNewUser,
        requiresProfileCompletion,
        credential.isHasPassword(),
        profileStatus);
  }

  /**
   * Complete OAuth2 profile with missing phone/email.
   */
  public OAuth2LoginResponse completeProfile(OAuth2CompleteProfileRequest request,
      Credential credential) {
    log.info("[OAuth2] Completing profile | credentialId={}", credential.getId());

    boolean updated = false;

    if ((credential.getPhoneNumber() == null || credential.getPhoneNumber().isBlank())
        && request.phoneNumber() != null && !request.phoneNumber().isBlank()) {

      if (credentialRepository.existsByPhoneNumber(request.phoneNumber())) {

        throw new BadRequestException("Phone number already linked to another account");
      }
      credential.setPhoneNumber(request.phoneNumber());
      updated = true;
    }

    if ((credential.getEmailAddress() == null || credential.getEmailAddress().isBlank())
        && request.email() != null && !request.email().isBlank()) {

      if (credentialRepository.existsByEmailAddress(request.email())) {
        throw new BadRequestException("Email already linked to another account");
      }
      credential.setEmailAddress(request.email());
      updated = true;
    }

    Customer customer = customerRepository.findByCredential(credential).orElse(null);
    if (customer != null && customer.getCountry() == null && request.country() != null) {
      customer.setCountry(request.country().getCode());
      if (customer.getPreferredCurrency() == null) {
        customer.setPreferredCurrency(SupportedCurrency.ETB);
      }
      customerRepository.save(customer);
      updated = true;
    }

    if (updated) {
      credentialRepository.save(credential);
    }

    ProfileCompletionStatus profileStatus = checkProfileStatus(credential);

    return new OAuth2LoginResponse(
        null, null, null, 0,
        false,
        profileStatus != ProfileCompletionStatus.COMPLETE,
        credential.isHasPassword(),
        profileStatus);
  }

  /**
   * Allow OAuth2 user to set a password.
   */
  public void setPassword(OAuth2SetPasswordRequest request, Credential credential) {
    log.info("[OAuth2] Setting password | credentialId={}", credential.getId());

    if (!request.password().equals(request.confirmPassword())) {
      throw new BadRequestException("Passwords do not match");
    }

    credential.setPasswordHash(passwordEncoder.encode(request.password()));
    credential.setHasPassword(true);
    credential.setLastPasswordChangeAt(LocalDateTime.now());

    credentialRepository.save(credential);
  }

  private Credential createNewOAuth2User(OAuth2LoginRequest request) {
    String tempPassword = UUID.randomUUID().toString();

    RoleDefinition customerRole = roleDefinitionRepository.findByCode(RoleCode.ROLE_CUSTOMER)
        .orElseThrow(() -> new IllegalStateException("ROLE_CUSTOMER not found"));
    Set<RoleDefinition> roles = new HashSet<>();
    roles.add(customerRole);

    Credential credential = new Credential();
    credential.setUserType(UserType.CUSTOMER);
    credential.setEmailAddress(request.email());
    credential.setPasswordHash(passwordEncoder.encode(tempPassword));
    credential.setStatus(UserStatus.ACTIVE);
    credential.setIdentityProvider(request.provider());
    credential.setIdentityProviderUserId(request.providerUserId());
    credential.setHasPassword(false);
    credential.setCreatedAt(LocalDateTime.now());
    credential.setCreatedBy("SYSTEM");
    credential.setRoles(roles);

    credential = credentialRepository.save(credential);

    String accountNumber = tillSequenceService.generateAccountNumber(UserType.CUSTOMER);
    String commissionAccount = accountNumber + "1";
    String referralCode = referralCodeUtils.generateCustomerReferralCode();

    Customer customer = new Customer();
    customer.setOldCustomerId(0L);
    customer.setCredential(credential);
    customer.setFirstName(request.firstName());
    customer.setLastName(request.lastName());
    customer.setAccountNumber(accountNumber);
    customer.setCommissionAccount(commissionAccount);
    customer.setReferralCode(referralCode);
    customer.setRegistrationChannel(request.channel());
    customer.setPreferredLanguage("en");
    customer.setPreferredCurrency(SupportedCurrency.ETB);
    customer.setCountry(SupportedCountry.ETHIOPIA.getCode());
    customer.setCreatedAt(LocalDateTime.now());
    customer.setCreatedBy("SYSTEM");

    customerRepository.save(customer);

    return credential;
  }

  private void updateExistingUser(Credential credential, OAuth2LoginRequest request) {
    boolean changed = false;
    if (credential.getLastDeviceId() == null && request.deviceId() != null) {
      credential.setLastDeviceId(request.deviceId());
      changed = true;
    }
    if (changed) {
      credentialRepository.save(credential);
    }
  }

  private void linkAccount(Credential credential, OAuth2LoginRequest request) {
    if (credential.getIdentityProvider() == IdentityProvider.LOCAL) {
      credential.setIdentityProvider(request.provider());
      credential.setIdentityProviderUserId(request.providerUserId());
      credentialRepository.save(credential);
    } else if (credential.getIdentityProvider() != request.provider()) {
      log.warn("User logging in with different provider | credentialId={} | requestedProvider={} | linkedProvider={}", 
          credential.getId(), request.provider(), credential.getIdentityProvider());
    }
  }

  private ProfileCompletionStatus checkProfileStatus(Credential credential) {
    Optional<Customer> customerOpt = customerRepository.findByCredential(credential);
    if (customerOpt.isEmpty()) {
      return ProfileCompletionStatus.COMPLETE;
    }

    Customer customer = customerOpt.get();
    String country = customer.getCountry();

    boolean hasEmail =
        credential.getEmailAddress() != null && !credential.getEmailAddress().isBlank();
    boolean hasPhone =
        credential.getPhoneNumber() != null && !credential.getPhoneNumber().isBlank();

    if (Objects.equals(country, SupportedCountry.ETHIOPIA.getCode())) {
      if (!hasPhone) {
        return hasEmail ? ProfileCompletionStatus.MISSING_PHONE
            : ProfileCompletionStatus.MISSING_BOTH;
      }
    } else {
      // Other countries - generally email is key
      if (!hasEmail) {
        return hasPhone ? ProfileCompletionStatus.MISSING_EMAIL
            : ProfileCompletionStatus.MISSING_BOTH;
      }
    }

    return ProfileCompletionStatus.COMPLETE;
  }
}
