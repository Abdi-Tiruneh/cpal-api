package com.commercepal.apiservice.orders.core.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Builder;

/**
 * DTO representing a single item within a customer order.
 * Contains detailed information about the product, tracking, and available actions.
 */
@Builder
@Schema(name = "CustomerOrderItemDto", description = "Detailed information about a single order item")
public record CustomerOrderItemDto(
    @Schema(description = "Unique sub-order identifier for this item", example = "SUB-ORD-123456", requiredMode = Schema.RequiredMode.REQUIRED)
    String subOrderNumber,

    @Schema(description = "Product name", example = "Wireless Bluetooth Headphones", requiredMode = Schema.RequiredMode.REQUIRED)
    String name,

    @Schema(description = "Product image URL", example = "https://example.com/images/product.jpg", requiredMode = Schema.RequiredMode.REQUIRED)
    String image,

    @Schema(description = "Product provider/vendor name", example = "AMAZON", requiredMode = Schema.RequiredMode.REQUIRED)
    String provider,

    @Schema(description = "Current status of the item", example = "SHIPPED", requiredMode = Schema.RequiredMode.REQUIRED)
    String status,

    @Schema(description = "Quantity of the item", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer quantity,

    @Schema(description = "Total price for this item", example = "4500.00", requiredMode = Schema.RequiredMode.REQUIRED)
    BigDecimal price,

    @Schema(description = "Tracking number for the shipment", example = "TRACK-123456", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String trackingNumber,

    @Schema(description = "Available actions for this item", requiredMode = Schema.RequiredMode.REQUIRED)
    OrderActionsDto actions
) {
}
