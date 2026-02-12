package com.commercepal.apiservice.orders.core.dto;

import com.commercepal.apiservice.orders.enums.OrderPriority;
import com.commercepal.apiservice.orders.enums.OrderStage;
import com.commercepal.apiservice.orders.enums.PaymentStatus;
import com.commercepal.apiservice.orders.enums.RefundStatus;
import com.commercepal.apiservice.shared.enums.Channel;
import com.commercepal.apiservice.shared.enums.SupportedCurrency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Advanced pagination, filtering, and sorting request DTO for admin order management.
 * <p>
 * Supports comprehensive filtering by order status, payment status, customer, dates, amounts, and
 * multi-level sorting for administrative order views.
 */
@ParameterObject
@Schema(description = "Advanced pagination, filtering, and sorting request for admin order management")
public record AdminOrderPageRequestDto(
    // ========== PAGINATION ==========
    @Schema(description = "Page number (0-based index)", example = "0")
    @Min(0)
    Integer page,

    @Schema(description = "Number of records per page", example = "20")
    @Min(1)
    Integer size,

    // ========== SORTING ==========
    @Schema(description = "Sort field (e.g., orderedAt, totalAmount, currentStage)", example = "orderedAt")
    String sortBy,

    @Schema(description = "Sort direction (ASC or DESC)", example = "DESC")
    Sort.Direction direction,

    // ========== SEARCH ==========
    @Schema(description = "Search by order number or customer name/email/phone", example = "ORD-2024")
    String searchQuery,

    // ========== ORDER FILTERS ==========
    @Schema(description = "Filter by customer ID")
    Long customerId,

    @Schema(description = "Filter by order stage", example = "PENDING")
    OrderStage currentStage,

    @Schema(description = "Filter by payment status", example = "PENDING")
    PaymentStatus paymentStatus,

    @Schema(description = "Filter by refund status", example = "NONE")
    RefundStatus refundStatus,

    @Schema(description = "Filter by order priority", example = "NORMAL")
    OrderPriority priority,

    @Schema(description = "Filter by platform/channel", example = "WEB")
    Channel platform,

    @Schema(description = "Filter by currency", example = "ETB")
    SupportedCurrency currency,

    @Schema(description = "Filter by agent ID")
    Long agentId,

    @Schema(description = "Filter by agent-initiated orders", example = "false")
    Boolean isAgentInitiated,

    // ========== DATE RANGE FILTERS ==========
    @Schema(description = "Filter orders placed after this date (ISO format: yyyy-MM-dd'T'HH:mm:ss)")
    LocalDateTime orderedAfter,

    @Schema(description = "Filter orders placed before this date (ISO format: yyyy-MM-dd'T'HH:mm:ss)")
    LocalDateTime orderedBefore,

    @Schema(description = "Filter orders completed after this date")
    LocalDateTime completedAfter,

    @Schema(description = "Filter orders completed before this date")
    LocalDateTime completedBefore,

    // ========== AMOUNT FILTERS ==========
    @Schema(description = "Minimum total amount", example = "100.00")
    BigDecimal minAmount,

    @Schema(description = "Maximum total amount", example = "10000.00")
    BigDecimal maxAmount
) {

  public AdminOrderPageRequestDto {
    // Set defaults
    if (page == null || page < 0) {
      page = 0;
    }
    if (size == null || size <= 0) {
      size = 20;
    }
    if (sortBy == null || sortBy.isBlank()) {
      sortBy = "orderedAt";
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
