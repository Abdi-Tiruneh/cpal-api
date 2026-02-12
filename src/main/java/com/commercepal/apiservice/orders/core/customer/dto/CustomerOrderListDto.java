package com.commercepal.apiservice.orders.core.customer.dto;

import com.commercepal.apiservice.orders.core.customer.CustomerOrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

/**
 * DTO representing a customer order in the order list view.
 * Contains essential order information for displaying orders in a paginated list.
 */
@Builder
@Schema(name = "CustomerOrderListDto", description = "Customer order summary for list view")
public record CustomerOrderListDto(
    @Schema(description = "Unique order identifier", example = "ORD-1702407470123-456", requiredMode = Schema.RequiredMode.REQUIRED)
    String orderNumber,

    @Schema(description = "Timestamp when the order was placed", example = "2025-01-15T10:30:00", requiredMode = Schema.RequiredMode.REQUIRED)
    LocalDateTime orderedAt,

    @Schema(description = "Current status of the order", example = "PROCESSING", requiredMode = Schema.RequiredMode.REQUIRED)
    CustomerOrderStatus status,

    @Schema(description = "Total amount of the order", example = "12500.00", requiredMode = Schema.RequiredMode.REQUIRED)
    BigDecimal totalAmount,

    @Schema(description = "Currency code", example = "ETB", requiredMode = Schema.RequiredMode.REQUIRED)
    String currency,

    @Schema(description = "Preview of order items (typically first item)", requiredMode = Schema.RequiredMode.REQUIRED)
    List<ItemPreviewDto> items,

    @Schema(description = "Available actions for this order", requiredMode = Schema.RequiredMode.REQUIRED)
    OrderActionsDto actions
) {
}
