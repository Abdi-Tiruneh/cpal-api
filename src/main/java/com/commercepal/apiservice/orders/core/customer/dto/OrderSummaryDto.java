package com.commercepal.apiservice.orders.core.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Builder;

/**
 * DTO representing the financial summary of an order.
 * Contains all pricing breakdown including subtotal, fees, discounts, and total.
 */
@Builder
@Schema(name = "OrderSummaryDto", description = "Financial summary and breakdown of order pricing")
public record OrderSummaryDto(
    @Schema(description = "Subtotal before discounts and fees", example = "10000.00", requiredMode = Schema.RequiredMode.REQUIRED)
    BigDecimal subtotal,

    @Schema(description = "Delivery fee", example = "500.00", requiredMode = Schema.RequiredMode.REQUIRED)
    BigDecimal delivery,

    @Schema(description = "Total discount amount applied", example = "1000.00", requiredMode = Schema.RequiredMode.REQUIRED)
    BigDecimal discount,

    @Schema(description = "Final total amount to be paid", example = "9500.00", requiredMode = Schema.RequiredMode.REQUIRED)
    BigDecimal total
) {
}
