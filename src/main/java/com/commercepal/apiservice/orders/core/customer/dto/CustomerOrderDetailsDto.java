package com.commercepal.apiservice.orders.core.customer.dto;

import com.commercepal.apiservice.orders.core.customer.CustomerOrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

/**
 * DTO representing complete order details for a customer.
 * Contains comprehensive information about the order including items, payment, and delivery details.
 */
@Builder
@Schema(name = "CustomerOrderDetailsDto", description = "Complete customer order details with all associated information")
public record CustomerOrderDetailsDto(
    @Schema(description = "Unique order identifier", example = "ORD-1702407470123-456", requiredMode = Schema.RequiredMode.REQUIRED)
    String orderNumber,

    @Schema(description = "Current status of the order", example = "PROCESSING", requiredMode = Schema.RequiredMode.REQUIRED)
    CustomerOrderStatus status,

    @Schema(description = "Timestamp when the order was placed", example = "2025-01-15T10:30:00", requiredMode = Schema.RequiredMode.REQUIRED)
    LocalDateTime orderedAt,

    @Schema(description = "Payment information for the order", requiredMode = Schema.RequiredMode.REQUIRED)
    PaymentDto payment,

    @Schema(description = "Delivery address information", requiredMode = Schema.RequiredMode.REQUIRED)
    DeliveryAddressDto deliveryAddress,

    @Schema(description = "List of all items in the order", requiredMode = Schema.RequiredMode.REQUIRED)
    List<CustomerOrderItemDto> items,

    @Schema(description = "Financial summary of the order", requiredMode = Schema.RequiredMode.REQUIRED)
    OrderSummaryDto summary
) {
}
