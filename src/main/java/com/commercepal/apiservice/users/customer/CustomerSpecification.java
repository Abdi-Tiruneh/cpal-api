package com.commercepal.apiservice.users.customer;

import com.commercepal.apiservice.users.credential.Credential;
import com.commercepal.apiservice.users.customer.dto.CustomerPageRequestDto;
import com.commercepal.apiservice.users.enums.UserStatus;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

/**
 * Advanced JPA Specification for dynamic Customer query construction. Supports complex filtering
 * across Customer and related Credential entities.
 */
@Slf4j
public class CustomerSpecification {

  private CustomerSpecification() {
    // Utility class - prevent instantiation
  }

  /**
   * Build comprehensive specification from CustomerPageRequestDto.
   *
   * @param dto the page request containing all filter criteria
   * @return Specification for dynamic query construction
   */
  public static Specification<Customer> buildSpecification(CustomerPageRequestDto dto) {
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      // JOIN: Credential entity (LEFT JOIN to handle potential null credentials)
      Join<Customer, Credential> credentialJoin = root.join("credential", JoinType.LEFT);

      // KEYWORD SEARCH: Multi-field OR search
      if (dto.normalizedKeyword() != null) {
        predicates.add(
            buildKeywordSearch(root, credentialJoin, criteriaBuilder, dto.normalizedKeyword()));
      }

      // CUSTOMER-SPECIFIC FILTERS
      addCustomerFilters(root, criteriaBuilder, predicates, dto);

      // ACCOUNT CREDENTIAL FILTERS
      addCredentialFilters(credentialJoin, criteriaBuilder, predicates, dto);

      // DATE RANGE FILTERS
      addDateRangeFilters(root, credentialJoin, criteriaBuilder, predicates, dto);

      // AUDIT FILTERS
      addAuditFilters(root, criteriaBuilder, predicates, dto);

      // Combine all predicates with AND
      if (predicates.isEmpty()) {
        return criteriaBuilder.conjunction(); // No filters = return all
      }

      log.debug("[CUSTOMER-SPEC] Built specification with {} predicates", predicates.size());
      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }

  /**
   * Build keyword search predicate across multiple fields. Customer fields: firstName, lastName,
   * accountNumber, city, stateProvince, customerNotes, adminNotes Credential fields: emailAddress,
   * phoneNumber
   */
  private static Predicate buildKeywordSearch(
      Root<Customer> root,
      Join<Customer, Credential> credentialJoin,
      CriteriaBuilder cb,
      String keyword) {

    String searchPattern = "%" + keyword + "%";

    List<Predicate> keywordPredicates = new ArrayList<>();

    // Customer entity fields
    keywordPredicates.add(cb.like(cb.lower(root.get("firstName")), searchPattern));
    keywordPredicates.add(cb.like(cb.lower(root.get("lastName")), searchPattern));
    keywordPredicates.add(cb.like(cb.lower(root.get("accountNumber")), searchPattern));
    keywordPredicates.add(cb.like(cb.lower(root.get("city")), searchPattern));
    keywordPredicates.add(cb.like(cb.lower(root.get("stateProvince")), searchPattern));
    keywordPredicates.add(cb.like(cb.lower(root.get("customerNotes")), searchPattern));
    keywordPredicates.add(cb.like(cb.lower(root.get("adminNotes")), searchPattern));
    keywordPredicates.add(cb.like(cb.lower(root.get("referralCode")), searchPattern));

    // Credential entity fields
    keywordPredicates.add(cb.like(cb.lower(credentialJoin.get("emailAddress")), searchPattern));
    keywordPredicates.add(cb.like(cb.lower(credentialJoin.get("phoneNumber")), searchPattern));

    return cb.or(keywordPredicates.toArray(new Predicate[0]));
  }

  /**
   * Add Customer-specific filter predicates.
   */
  private static void addCustomerFilters(
      Root<Customer> root,
      CriteriaBuilder cb,
      List<Predicate> predicates,
      CustomerPageRequestDto dto) {

    // COUNTRY FILTER
    if (dto.country() != null) {
      predicates.add(cb.equal(root.get("country"), dto.country()));
    }

    // CITY FILTER (partial match)
    if (dto.city() != null && !dto.city().isBlank()) {
      predicates.add(cb.like(cb.lower(root.get("city")), "%" + dto.city().toLowerCase() + "%"));
    }

    // STATE/PROVINCE FILTER (partial match)
    if (dto.stateProvince() != null && !dto.stateProvince().isBlank()) {
      predicates.add(cb.like(cb.lower(root.get("stateProvince")),
          "%" + dto.stateProvince().toLowerCase() + "%"));
    }

    // REGISTRATION CHANNEL FILTER
    if (dto.registrationChannel() != null) {
      predicates.add(cb.equal(root.get("registrationChannel"), dto.registrationChannel()));
    }
  }

  /**
   * Add Credential-specific filter predicates.
   */
  private static void addCredentialFilters(
      Join<Customer, Credential> credentialJoin,
      CriteriaBuilder cb,
      List<Predicate> predicates,
      CustomerPageRequestDto dto) {

    // STATUS FILTER
    if (dto.status() != null) {
      predicates.add(cb.equal(credentialJoin.get("status"), dto.status()));
    }

    // EMAIL VERIFIED FILTER
    if (dto.isEmailVerified() != null) {
      predicates.add(cb.equal(credentialJoin.get("isEmailVerified"), dto.isEmailVerified()));
    }

    // PHONE VERIFIED FILTER
    if (dto.isPhoneVerified() != null) {
      predicates.add(cb.equal(credentialJoin.get("isPhoneVerified"), dto.isPhoneVerified()));
    }

    // MFA ENABLED FILTER
    if (dto.mfaEnabled() != null) {
      predicates.add(cb.equal(credentialJoin.get("mfaEnabled"), dto.mfaEnabled()));
    }

    // LOCKED FILTER
    if (dto.isLocked() != null) {
      if (dto.isLocked()) {
        // Locked = lockedUntil is not null and in the future
        predicates.add(cb.isNotNull(credentialJoin.get("lockedUntil")));
        predicates.add(
            cb.greaterThan(credentialJoin.get("lockedUntil"), LocalDateTime.now()));
      } else {
        // Not locked = lockedUntil is null or in the past
        predicates.add(cb.or(
            cb.isNull(credentialJoin.get("lockedUntil")),
            cb.lessThanOrEqualTo(credentialJoin.get("lockedUntil"), LocalDateTime.now())
        ));
      }
    }
  }

  /**
   * Add date range filter predicates.
   */
  private static void addDateRangeFilters(
      Root<Customer> root,
      Join<Customer, Credential> credentialJoin,
      CriteriaBuilder cb,
      List<Predicate> predicates,
      CustomerPageRequestDto dto) {

    // Customer creation/update dates
    if (dto.createdAfter() != null) {
      predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), dto.createdAfter()));
    }

    if (dto.createdBefore() != null) {
      predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), dto.createdBefore()));
    }

    if (dto.updatedAfter() != null) {
      predicates.add(cb.greaterThanOrEqualTo(root.get("updatedAt"), dto.updatedAfter()));
    }

    if (dto.updatedBefore() != null) {
      predicates.add(cb.lessThanOrEqualTo(root.get("updatedAt"), dto.updatedBefore()));
    }

    // Last sign-in dates (from Credential)
    if (dto.lastSignedInAfter() != null) {
      predicates.add(cb.greaterThanOrEqualTo(
          credentialJoin.get("lastSignedInAt"), dto.lastSignedInAfter()));
    }

    if (dto.lastSignedInBefore() != null) {
      predicates.add(cb.lessThanOrEqualTo(
          credentialJoin.get("lastSignedInAt"), dto.lastSignedInBefore()));
    }
  }

  /**
   * Add audit filter predicates.
   */
  private static void addAuditFilters(
      Root<Customer> root,
      CriteriaBuilder cb,
      List<Predicate> predicates,
      CustomerPageRequestDto dto) {

    if (dto.isDeleted() != null) {
      predicates.add(cb.equal(root.get("isDeleted"), dto.isDeleted()));
    }
  }

  // ========== CONVENIENCE SPECIFICATIONS ==========

  /**
   * Find customer by ID.
   */
  public static Specification<Customer> withId(Long id) {
    return (root, query, cb) -> cb.equal(root.get("id"), id);
  }

  /**
   * Find customer by account number.
   */
  public static Specification<Customer> withAccountNumber(String accountNumber) {
    return (root, query, cb) -> cb.equal(root.get("accountNumber"), accountNumber);
  }

  /**
   * Find customer by referral code.
   */
  public static Specification<Customer> withReferralCode(String referralCode) {
    return (root, query, cb) -> cb.equal(root.get("referralCode"), referralCode);
  }

  /**
   * Find non-deleted customers.
   */
  public static Specification<Customer> notDeleted() {
    return (root, query, cb) -> cb.equal(root.get("isDeleted"), false);
  }

  /**
   * Find customers with active status.
   */
  public static Specification<Customer> withActiveStatus() {
    return (root, query, cb) -> {
      Join<Customer, Credential> credentialJoin = root.join("credential", JoinType.LEFT);
      return cb.equal(credentialJoin.get("status"), UserStatus.ACTIVE);
    };
  }

  /**
   * Find customers with verified email.
   */
  public static Specification<Customer> withVerifiedEmail() {
    return (root, query, cb) -> {
      Join<Customer, Credential> credentialJoin = root.join("credential", JoinType.LEFT);
      return cb.equal(credentialJoin.get("isEmailVerified"), true);
    };
  }

  /**
   * Find customers with verified phone.
   */
  public static Specification<Customer> withVerifiedPhone() {
    return (root, query, cb) -> {
      Join<Customer, Credential> credentialJoin = root.join("credential", JoinType.LEFT);
      return cb.equal(credentialJoin.get("isPhoneVerified"), true);
    };
  }

  /**
   * Find customers by country.
   */
  public static Specification<Customer> withCountry(
      com.commercepal.apiservice.shared.enums.SupportedCountry country) {
    return (root, query, cb) -> cb.equal(root.get("country"), country);
  }

  /**
   * Find customers by registration channel.
   */
  public static Specification<Customer> withRegistrationChannel(
      com.commercepal.apiservice.shared.enums.Channel channel) {
    return (root, query, cb) -> cb.equal(root.get("registrationChannel"), channel);
  }

  /**
   * Find customers created within a date range.
   */
  public static Specification<Customer> createdBetween(LocalDateTime start, LocalDateTime end) {
    return (root, query, cb) -> cb.between(root.get("createdAt"), start, end);
  }

  /**
   * Find customers who signed in within a date range.
   */
  public static Specification<Customer> lastSignedInBetween(LocalDateTime start,
      LocalDateTime end) {
    return (root, query, cb) -> {
      Join<Customer, Credential> credentialJoin = root.join("credential", JoinType.LEFT);
      return cb.between(credentialJoin.get("lastSignedInAt"), start, end);
    };
  }

}
