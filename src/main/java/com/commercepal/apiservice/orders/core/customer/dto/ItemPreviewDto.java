package com.commercepal.apiservice.orders.core.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * DTO representing a preview of an order item.
 * Used for displaying a quick summary of items in order lists.
 */
@Builder
@Schema(name = "ItemPreviewDto", description = "Preview information for an order item")
public record ItemPreviewDto(
    @Schema(description = "Product name", example = "Wireless Bluetooth Headphones", requiredMode = Schema.RequiredMode.REQUIRED)
    String name,

    @Schema(description = "Product image URL", example = "https://example.com/images/product.jpg", requiredMode = Schema.RequiredMode.REQUIRED)
    String image,

    @Schema(description = "Quantity of the item", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer quantity,

    @Schema(description = "Current status of the item", example = "SHIPPED", requiredMode = Schema.RequiredMode.REQUIRED)
    String status
) {
}
