package com.commercepal.apiservice.utils;

import com.commercepal.apiservice.promotions.affiliate.user.Affiliate;
import com.commercepal.apiservice.promotions.affiliate.user.AffiliateRepository;
import com.commercepal.apiservice.shared.exceptions.security.ForbiddenException;
import com.commercepal.apiservice.shared.exceptions.security.UnauthorizedException;
import com.commercepal.apiservice.users.credential.Credential;
import com.commercepal.apiservice.users.credential.CredentialRepository;
import com.commercepal.apiservice.users.customer.Customer;
import com.commercepal.apiservice.users.customer.CustomerRepository;
import com.commercepal.apiservice.users.role.RoleCode;
import com.commercepal.apiservice.users.staff.Staff;
import com.commercepal.apiservice.users.staff.StaffRepository;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Service for accessing and validating the current authenticated user in the system. Provides
 * comprehensive user context retrieval for different user types.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CurrentUserService {

  private final CredentialRepository credentialRepository;
  private final CustomerRepository customerRepository;
  private final StaffRepository staffRepository;
  private final AffiliateRepository affiliateRepository;

  /**
   * Get the current authenticated user (Credential) from Spring Security context.
   *
   * @return authenticated Credential
   * @throws UnauthorizedException if not authenticated
   * @throws ForbiddenException    if current user context cannot be resolved
   */
  public Credential getCurrentUser() {
    Authentication authentication = getAuthentication();
    Object principal = authentication.getPrincipal();

    if (principal instanceof Credential credential) {
      log.info("Current user retrieved | CurrentUserService | getCurrentUser | identifier={}",
          credential.getUsername());
      return credential;
    }

    String identifier = authentication.getName();
    log.info("Fetching current user | CurrentUserService | getCurrentUser | identifier={}",
        identifier);

    return credentialRepository.findByIdentifier(identifier)
        .orElseThrow(() -> {
          log.warn(
              "Account credential not found | CurrentUserService | getCurrentUser | identifier={}",
              identifier);
          return new ForbiddenException();
        });
  }

  /**
   * Get current staff profile entity for the authenticated user.
   *
   * @return Staff entity
   * @throws UnauthorizedException if not authenticated
   * @throws ForbiddenException    if current user context cannot be resolved
   */
  public Staff getCurrentStaff() {
    Credential credential = getCurrentUser();

    log.info("Fetching current staff | CurrentUserService | getCurrentStaff | credentialId={}",
        credential.getId());

    return staffRepository.findByCredentialIdAndIsDeletedFalse(credential.getId())
        .orElseThrow(() -> {
          log.warn(
              "Staff profile not found | CurrentUserService | getCurrentStaff | credentialId={}",
              credential.getId());
          return new ForbiddenException();
        });
  }

  /**
   * Get current customer profile entity for the authenticated user.
   * Eagerly loads the credential to prevent LazyInitializationException.
   *
   * @return Customer entity with credential loaded
   * @throws UnauthorizedException if not authenticated
   * @throws ForbiddenException    if current user context cannot be resolved
   */
  public Customer getCurrentCustomer() {
    Credential credential = getCurrentUser();

    log.info(
        "Fetching current customer | CurrentUserService | getCurrentCustomer | credentialId={}",
        credential.getId());

    return customerRepository.findByCredentialIdWithCredential(credential.getId())
        .orElseThrow(() -> {
          log.warn(
              "Customer profile not found | CurrentUserService | getCurrentCustomer | credentialId={}",
              credential.getId());
          return new ForbiddenException();
        });
  }

  /**
   * Get current affiliate profile entity for the authenticated user.
   *
   * @return Affiliate entity
   * @throws UnauthorizedException if not authenticated
   * @throws ForbiddenException    if current user context cannot be resolved
   */
  public Affiliate getCurrentAffiliate() {
    Credential credential = getCurrentUser();

    log.info(
        "Fetching current affiliate | CurrentUserService | getCurrentAffiliate | credentialId={}",
        credential.getId());

    return affiliateRepository.findByCredential_Id(credential.getId())
        .orElseThrow(() -> {
          log.warn(
              "Affiliate profile not found | CurrentUserService | getCurrentAffiliate | credentialId={}",
              credential.getId());
          return new ForbiddenException();
        });
  }

  /**
   * Get the credential ID of the current authenticated user.
   *
   * @return credential ID
   * @throws UnauthorizedException if not authenticated
   * @throws ForbiddenException    if current user context cannot be resolved
   */
  public Long getCurrentUserId() {
    Authentication authentication = getAuthentication();
    Object principal = authentication.getPrincipal();

    if (principal instanceof Credential credential) {
      return credential.getId();
    }

    // Fallback: fetch from database if principal is not Credential
    String identifier = authentication.getName();
    return credentialRepository.findByIdentifier(identifier)
        .map(Credential::getId)
        .orElseThrow(ForbiddenException::new);
  }

  /**
   * Get the username/identifier of the current authenticated user.
   *
   * @return username string
   * @throws UnauthorizedException if not authenticated
   */
  public String getCurrentUsername() {
    Authentication authentication = getAuthentication();
    Object principal = authentication.getPrincipal();

    if (principal instanceof Credential credential) {
      return credential.getUsername();
    }

    return authentication.getName();
  }

  /**
   * Check if current principal is authenticated.
   *
   * @return true if authenticated
   */
  public boolean isAuthenticated() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication != null
        && authentication.isAuthenticated()
        && !(authentication instanceof AnonymousAuthenticationToken);
  }

  /**
   * Check if current principal has any of the specified roles.
   *
   * @param roles varargs of RoleCode to check
   * @return true if principal has at least one of the specified roles
   */
  public boolean hasAnyRole(RoleCode... roles) {
    if (roles == null || roles.length == 0) {
      return false;
    }

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return false;
    }

    return authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .anyMatch(authority -> Arrays.stream(roles)
            .anyMatch(role -> authority.equals(role.name())));
  }

  /**
   * Ensure the current principal has at least one of the specified roles. Throws ForbiddenException
   * if the principal does not have any of the required roles.
   *
   * @param roles varargs of RoleCode that are allowed
   * @throws ForbiddenException if principal lacks all specified roles
   */
  public void ensureHasAnyRole(RoleCode... roles) {
    if (!hasAnyRole(roles)) {
      log.warn(
          "Access denied - insufficient roles | CurrentUserService | ensureHasAnyRole");
      throw new ForbiddenException();
    }
  }

  /**
   * Get the current authentication from Spring Security context.
   *
   * @return Authentication object
   * @throws UnauthorizedException if not authenticated
   */
  private Authentication getAuthentication() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null
        || !authentication.isAuthenticated()
        || authentication instanceof AnonymousAuthenticationToken) {
      log.warn("Unauthenticated access attempt | CurrentUserService | getAuthentication");
      throw new UnauthorizedException();
    }

    return authentication;
  }
}
