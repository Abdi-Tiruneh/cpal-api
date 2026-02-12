package com.commercepal.apiservice.orders.core.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * DTO representing available actions for an order or order item.
 * Indicates which actions the customer can perform on the order.
 */
@Builder
@Schema(name = "OrderActionsDto", description = "Available actions for an order or order item")
public record OrderActionsDto(
    @Schema(description = "Whether the order can be paid", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    boolean canPay,

    @Schema(description = "Whether tracking information is available", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    boolean canTrack
) {
}
