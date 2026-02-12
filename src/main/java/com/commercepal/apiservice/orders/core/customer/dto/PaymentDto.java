package com.commercepal.apiservice.orders.core.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;

/**
 * DTO representing payment information for an order.
 * Contains payment status and confirmation timestamp.
 */
@Builder
@Schema(name = "PaymentDto", description = "Payment information and status for an order")
public record PaymentDto(
    @Schema(description = "Payment status", example = "PAID", requiredMode = Schema.RequiredMode.REQUIRED)
    String status,

    @Schema(description = "Timestamp when payment was confirmed", example = "2025-01-15T10:35:00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    LocalDateTime paidAt
) {
}
