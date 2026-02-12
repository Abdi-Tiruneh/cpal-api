package com.commercepal.apiservice.payments.oderPayment.dto;

import com.commercepal.apiservice.orders.enums.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Pagination and filter request DTO for order payment listing. Filter fields align with
 * {@link com.commercepal.apiservice.payments.oderPayment.OrderPayment} and joined
 * {@link com.commercepal.apiservice.orders.core.model.Order} fields.
 */
@ParameterObject
@Schema(description = "Pagination and filter request for order payments")
public record OrderPaymentPageRequest(
    @Schema(description = "Page number (0-based index)", example = "0")
    @Min(0)
    Integer page,

    @Schema(description = "Number of records per page", example = "15")
    @Min(1)
    Integer size,

    @Schema(description = "Filter by payment reference (OrderPayment.reference)", example = "CP-123456789")
    String paymentReference,

    @Schema(description = "Filter by order number (Order.orderNumber)", example = "ORD-2024-001")
    String orderReference,

    @Schema(description = "Filter by payment status (OrderPayment.status)", example = "PENDING")
    PaymentStatus status,

    @Schema(description = "Filter by gateway (OrderPayment.gateway)", example = "TELEBIRR")
    String gateway,

    @Schema(description = "Filter by customer email (Customer.credential.emailAddress)", example = "customer@example.com")
    String customerEmail,

    @Schema(description = "Filter by customer phone number (Customer.credential.phoneNumber)", example = "+251912345678")
    String customerPhoneNumber,

    @Schema(description = "Filter by payment amount (OrderPayment.amount). Searches with +/- 0.5 tolerance", example = "100.00")
    BigDecimal amount,

    @Schema(description = "Sort field: createdAt, resolvedAt, amount, reference, status", example = "createdAt")
    String sortBy,

    @Schema(description = "Sort direction (ASC or DESC)", example = "DESC")
    Sort.Direction direction
) {

  public OrderPaymentPageRequest {
    if (page == null || page < 0) {
      page = 0;
    }
    if (size == null || size <= 0) {
      size = 15;
    }
    if (sortBy == null || sortBy.isBlank()) {
      sortBy = "createdAt";
    }
    if (direction == null) {
      direction = Sort.Direction.DESC;
    }
  }

  public Pageable toPageable() {
    Sort sort = Sort.by(direction, sortBy);
    return PageRequest.of(page, size, sort);
  }
}
