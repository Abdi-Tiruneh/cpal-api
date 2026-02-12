package com.commercepal.apiservice.orders.core.specification;

import com.commercepal.apiservice.orders.core.dto.AdminOrderPageRequestDto;
import com.commercepal.apiservice.orders.core.model.Order;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

/**
 * JPA Specification for dynamic Order query construction. Supports
 * comprehensive filtering for
 * admin order management.
 */
@Slf4j
public class OrderSpecification {

  private OrderSpecification() {
    // Utility class - prevent instantiation
  }

  /**
   * Build comprehensive specification from AdminOrderPageRequestDto.
   *
   * @param dto the page request containing all filter criteria
   * @return Specification for dynamic query construction
   */
  public static Specification<Order> buildSpecification(AdminOrderPageRequestDto dto) {
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      // JOIN: Customer entity for customer-related filters
      Join<Order, com.commercepal.apiservice.users.customer.Customer> customerJoin = root.join("customer",
          JoinType.LEFT);

      // SEARCH QUERY: Order number or customer name/email/phone
      if (StringUtils.hasText(dto.searchQuery())) {
        String searchPattern = "%" + dto.searchQuery().toLowerCase() + "%";
        Predicate orderNumberPredicate = criteriaBuilder.like(
            criteriaBuilder.lower(root.get("orderNumber")), searchPattern);

        Predicate customerNamePredicate = criteriaBuilder.or(
            criteriaBuilder.like(
                criteriaBuilder.lower(customerJoin.get("firstName")), searchPattern),
            criteriaBuilder.like(
                criteriaBuilder.lower(customerJoin.get("lastName")), searchPattern));

        predicates.add(criteriaBuilder.or(orderNumberPredicate, customerNamePredicate));
      }

      // CUSTOMER ID FILTER
      if (dto.customerId() != null) {
        predicates.add(criteriaBuilder.equal(customerJoin.get("id"), dto.customerId()));
      }

      // ORDER STAGE FILTER
      if (dto.currentStage() != null) {
        predicates.add(criteriaBuilder.equal(root.get("currentStage"), dto.currentStage()));
      }

      // PAYMENT STATUS FILTER
      if (dto.paymentStatus() != null) {
        predicates.add(criteriaBuilder.equal(root.get("paymentStatus"), dto.paymentStatus()));
      }

      // REFUND STATUS FILTER
      if (dto.refundStatus() != null) {
        predicates.add(criteriaBuilder.equal(root.get("refundStatus"), dto.refundStatus()));
      }

      // PRIORITY FILTER
      if (dto.priority() != null) {
        predicates.add(criteriaBuilder.equal(root.get("priority"), dto.priority()));
      }

      // PLATFORM FILTER
      if (dto.platform() != null) {
        predicates.add(criteriaBuilder.equal(root.get("platform"), dto.platform()));
      }

      // CURRENCY FILTER
      if (dto.currency() != null) {
        predicates.add(criteriaBuilder.equal(root.get("orderCurrency"), dto.currency()));
      }

      // AGENT FILTERS
      if (dto.agentId() != null) {
        predicates.add(criteriaBuilder.equal(root.get("agentId"), dto.agentId()));
      }
      if (dto.isAgentInitiated() != null) {
        predicates.add(criteriaBuilder.equal(root.get("isAgentInitiated"), dto.isAgentInitiated()));
      }

      // DATE RANGE FILTERS
      if (dto.orderedAfter() != null) {
        predicates.add(
            criteriaBuilder.greaterThanOrEqualTo(root.get("orderedAt"), dto.orderedAfter()));
      }
      if (dto.orderedBefore() != null) {
        predicates.add(
            criteriaBuilder.lessThanOrEqualTo(root.get("orderedAt"), dto.orderedBefore()));
      }
      if (dto.completedAfter() != null) {
        predicates.add(
            criteriaBuilder.greaterThanOrEqualTo(root.get("completedAt"), dto.completedAfter()));
      }
      if (dto.completedBefore() != null) {
        predicates.add(
            criteriaBuilder.lessThanOrEqualTo(root.get("completedAt"), dto.completedBefore()));
      }

      // AMOUNT FILTERS
      if (dto.minAmount() != null) {
        predicates.add(
            criteriaBuilder.greaterThanOrEqualTo(root.get("totalAmount"), dto.minAmount()));
      }
      if (dto.maxAmount() != null) {
        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("totalAmount"), dto.maxAmount()));
      }

      // Combine all predicates with AND
      if (predicates.isEmpty()) {
        return criteriaBuilder.conjunction(); // No filters = return all
      }

      log.debug("[ORDER-SPEC] Built specification with {} predicates", predicates.size());
      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }
}
