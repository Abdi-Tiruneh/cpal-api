package com.commercepal.apiservice.users.staff;

import com.commercepal.apiservice.users.staff.dto.StaffPageRequest;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

/**
 * JPA Specification for dynamic Staff query construction from {@link StaffPageRequest}.
 * Uses entity field names from {@link Staff}. Always excludes soft-deleted staff.
 */
public final class StaffSpecification {

  private StaffSpecification() {
    // Utility class
  }

  /**
   * Build specification from StaffPageRequest. Filters by: search (firstName, lastName, employeeId),
   * department, status. Always applies isDeleted = false.
   *
   * @param request the page request with optional filters
   * @return Specification for dynamic query construction
   */
  public static Specification<Staff> buildSpecification(StaffPageRequest request) {
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      // Exclude soft-deleted
      predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));

      // Search: firstName, lastName, employeeId (case-insensitive like)
      if (StringUtils.hasText(request.search())) {
        String term = request.search().trim().toLowerCase();
        String pattern = "%" + term + "%";
        Predicate firstName = criteriaBuilder.like(
            criteriaBuilder.lower(root.get("firstName")), pattern);
        Predicate lastName = criteriaBuilder.like(
            criteriaBuilder.lower(root.get("lastName")), pattern);
        Predicate employeeId = criteriaBuilder.like(
            criteriaBuilder.lower(root.get("employeeId")), pattern);
        predicates.add(criteriaBuilder.or(firstName, lastName, employeeId));
      }

      // Department
      if (request.department() != null) {
        predicates.add(criteriaBuilder.equal(root.get("department"), request.department()));
      }

      // Status
      if (request.status() != null) {
        predicates.add(criteriaBuilder.equal(root.get("status"), request.status()));
      }

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }
}
