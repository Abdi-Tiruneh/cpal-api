package com.commercepal.apiservice.shared.config;

import java.util.Optional;
import org.springframework.data.domain.AuditorAware;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Implementation of AuditorAware to provide the current auditor (user) information for JPA
 * auditing. This class automatically populates the @CreatedBy and @LastModifiedBy fields in
 * entities that extend BaseAuditEntity.
 */
@Component
public class AuditAwareImpl implements AuditorAware<String> {

  /**
   * Returns the current auditor (user) based on the security context. This method is called
   * automatically by Spring Data JPA when saving entities.
   *
   * @return Optional containing the current user's identifier, or "system" if no user is
   * authenticated
   */
  @Override
  @NonNull
  public Optional<String> getCurrentAuditor() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      return Optional.of("system");
    }

    // Handle anonymous users
    if ("anonymousUser".equals(authentication.getPrincipal())) {
      return Optional.of("anonymous");
    }

    // For JWT or other token-based authentication, extract username
    String currentUser = authentication.getName();

    // If the principal is a UserDetails object, you might want to extract more specific info
    // Example: if (authentication.getPrincipal() instanceof UserDetails) {
    //     UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    //     currentUser = userDetails.getUsername();
    // }

    return Optional.of(currentUser != null && !currentUser.isEmpty() ? currentUser : "system");
  }
}
