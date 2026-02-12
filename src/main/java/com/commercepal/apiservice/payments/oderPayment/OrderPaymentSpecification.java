package com.commercepal.apiservice.payments.oderPayment;

import com.commercepal.apiservice.payments.oderPayment.dto.OrderPaymentPageRequest;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

/**
 * JPA Specification for dynamic OrderPayment query construction from
 * {@link OrderPaymentPageRequest}. Uses entity field names from {@link OrderPayment} and joined
 * {@link com.commercepal.apiservice.orders.core.model.Order}.
 */
public final class OrderPaymentSpecification {

  private OrderPaymentSpecification() {
    // Utility class
  }

  /**
   * Build specification from OrderPaymentPageRequest. Filters by: paymentReference (reference),
   * orderReference (order.orderNumber), status, gateway, customerEmail
   * (customer.credential.emailAddress), customerPhoneNumber (customer.credential.phoneNumber),
   * amount (with +/- 0.5 tolerance).
   *
   * @param request the page request with optional filters
   * @return Specification for dynamic query construction
   */
  public static Specification<OrderPayment> buildSpecification(OrderPaymentPageRequest request) {
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      // OrderPayment.reference (payment_reference)
      if (StringUtils.hasText(request.paymentReference())) {
        String pattern = "%" + request.paymentReference().trim() + "%";
        predicates.add(
            criteriaBuilder.like(
                criteriaBuilder.lower(root.get("reference")),
                pattern.toLowerCase()));
      }

      // OrderPayment.status
      if (request.status() != null) {
        predicates.add(criteriaBuilder.equal(root.get("status"), request.status()));
      }

      // OrderPayment.gateway
      if (StringUtils.hasText(request.gateway())) {
        String pattern = "%" + request.gateway().trim() + "%";
        predicates.add(
            criteriaBuilder.like(
                criteriaBuilder.lower(root.get("gateway")),
                pattern.toLowerCase()));
      }

      // OrderPayment.amount (with +/- 0.5 tolerance)
      if (request.amount() != null) {
        BigDecimal tolerance = new BigDecimal("0.5");
        BigDecimal minAmount = request.amount().subtract(tolerance);
        BigDecimal maxAmount = request.amount().add(tolerance);
        predicates.add(
            criteriaBuilder.between(
                root.get("amount"),
                minAmount,
                maxAmount));
      }

      // Order join only when filtering by order number
      if (StringUtils.hasText(request.orderReference())) {
        var orderJoin = root.join("order", JoinType.INNER);
        String pattern = "%" + request.orderReference().trim() + "%";
        predicates.add(
            criteriaBuilder.like(
                criteriaBuilder.lower(orderJoin.get("orderNumber")),
                pattern.toLowerCase()));
      }

      // Customer join for email and phone number filtering (only when needed)
      if (StringUtils.hasText(request.customerEmail()) || StringUtils.hasText(
          request.customerPhoneNumber())) {
        var customerJoin = root.join("customer", JoinType.INNER);
        var credentialJoin = customerJoin.join("credential", JoinType.INNER);

        // Customer email (customer.credential.emailAddress)
        if (StringUtils.hasText(request.customerEmail())) {
          String pattern = "%" + request.customerEmail().trim() + "%";
          predicates.add(
              criteriaBuilder.like(
                  criteriaBuilder.lower(credentialJoin.get("emailAddress")),
                  pattern.toLowerCase()));
        }

        // Customer phone number (customer.credential.phoneNumber)
        // Extract digits from search term and search for them in phone number field
        // This will match phone numbers regardless of formatting (+251912345678, 251912345678, etc.)
        if (StringUtils.hasText(request.customerPhoneNumber())) {
          // Extract only digits from search term (remove +, spaces, dashes, etc.)
          String digitsOnly = request.customerPhoneNumber().trim().replaceAll("[^0-9]", "");

          if (!digitsOnly.isEmpty()) {
            // Use LIKE to find the digits in sequence - works regardless of + prefix or formatting
            // Example: searching "251112345678" will match "+251112345678" or "251112345678"
            String pattern = "%" + digitsOnly + "%";
            predicates.add(
                criteriaBuilder.like(
                    credentialJoin.get("phoneNumber"),
                    pattern));
          }
        }
      }

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }
}
