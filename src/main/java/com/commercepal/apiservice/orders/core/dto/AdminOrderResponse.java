package com.commercepal.apiservice.orders.core.dto;

import com.commercepal.apiservice.orders.enums.OrderPriority;
import com.commercepal.apiservice.orders.enums.OrderStage;
import com.commercepal.apiservice.orders.enums.PaymentStatus;
import com.commercepal.apiservice.orders.enums.RefundStatus;
import com.commercepal.apiservice.orders.tracking.dto.DeliveryAddressSummary;
import com.commercepal.apiservice.orders.tracking.dto.OrderItemSummary;
import com.commercepal.apiservice.shared.enums.Channel;
import com.commercepal.apiservice.shared.enums.SupportedCurrency;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

/**
 * Comprehensive admin order response DTO.
 * <p>
 * Provides complete order information for administrative views including all financial details,
 * status information, customer information, and audit fields.
 */
@Builder
@Schema(
    name = "AdminOrderResponse",
    description = "Comprehensive order information for administrative views with complete order details"
)
public record AdminOrderResponse(
    @Schema(description = "Order ID", example = "123")
    Long orderId,

    @Schema(description = "Unique order number", example = "ORD-20240101-ABC12345")
    String orderNumber,

    @Schema(description = "Customer ID", example = "456")
    Long customerId,

    @Schema(description = "Customer account number", example = "ACC-123456")
    String customerAccountNumber,

    @Schema(description = "Customer full name", example = "John Doe")
    String customerName,

    @Schema(description = "Customer email address", example = "john.doe@example.com")
    String customerEmail,

    @Schema(description = "Customer phone number", example = "+251911223344")
    String customerPhone,

    @Schema(description = "Platform/channel where order was placed", example = "WEB")
    Channel platform,

    @Schema(description = "Order priority level", example = "NORMAL")
    OrderPriority priority,

    @Schema(description = "Order currency", example = "ETB")
    SupportedCurrency currency,

    @Schema(description = "Order subtotal", example = "1500.00")
    BigDecimal subtotal,

    @Schema(description = "Tax amount", example = "150.00")
    BigDecimal taxAmount,

    @Schema(description = "Delivery fee", example = "100.00")
    BigDecimal deliveryFee,

    @Schema(description = "Discount amount", example = "50.00")
    BigDecimal discountAmount,

    @Schema(description = "Additional charges", example = "25.00")
    BigDecimal additionalCharges,

    @Schema(description = "Total amount", example = "1725.00")
    BigDecimal totalAmount,

    @Schema(description = "Current order stage", example = "PENDING")
    OrderStage currentStage,

    @Schema(description = "Payment status", example = "PENDING")
    PaymentStatus paymentStatus,

    @Schema(description = "Refund status", example = "NONE")
    RefundStatus refundStatus,

    @Schema(description = "Refunded amount", example = "0.00")
    BigDecimal refundedAmount,

    @Schema(description = "Total number of items in order", example = "3")
    Integer totalItemsCount,

    @Schema(description = "List of order items")
    List<OrderItemSummary> items,

    @Schema(description = "Delivery address information")
    DeliveryAddressSummary deliveryAddress,

    @Schema(description = "When order was placed")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime orderedAt,

    @Schema(description = "When order was completed")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime completedAt,

    @Schema(description = "When order was cancelled")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime cancelledAt,

    @Schema(description = "Cancellation reason")
    String cancellationReason,

    @Schema(description = "When payment was confirmed")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime paymentConfirmedAt,

    @Schema(description = "Payment reference/transaction ID")
    String paymentReference,

    @Schema(description = "When refund was initiated")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime refundInitiatedAt,

    @Schema(description = "When refund was completed")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime refundCompletedAt,

    @Schema(description = "Whether order was initiated by an agent", example = "false")
    Boolean isAgentInitiated,

    @Schema(description = "Agent ID if applicable")
    Long agentId,

    @Schema(description = "Cart ID if order came from cart")
    Long cartId,

    @Schema(description = "Created at timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdAt,

    @Schema(description = "Updated at timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime updatedAt
) {

}
