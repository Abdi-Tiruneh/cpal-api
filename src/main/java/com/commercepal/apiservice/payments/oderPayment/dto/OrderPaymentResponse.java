package com.commercepal.apiservice.payments.oderPayment.dto;

import com.commercepal.apiservice.orders.enums.PaymentStatus;
import com.commercepal.apiservice.shared.enums.SupportedCurrency;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;

/**
 * Response DTO for OrderPayment. Maps from
 * {@link com.commercepal.apiservice.payments.oderPayment.OrderPayment} and related
 * {@link com.commercepal.apiservice.orders.core.model.Order} fields.
 */
@Builder
@Schema(
    name = "OrderPaymentResponse",
    description = "Payment response containing payment details, status, and related order information"
)
public record OrderPaymentResponse(
    @Schema(description = "Payment reference (OrderPayment.reference)", example = "CP123456789")
    String reference,

    @Schema(description = "Gateway transaction reference (OrderPayment.gatewayReference)", example = "GW-TXN-123456")
    String gatewayReference,

    @Schema(description = "Payment gateway (OrderPayment.gateway)", example = "TELEBIRR")
    String gateway,

    @Schema(description = "Account number (OrderPayment.accountNumber)", example = "0911234567")
    String accountNumber,

    @Schema(description = "Payment amount (OrderPayment.amount)", example = "1500.00")
    BigDecimal amount,

    @Schema(description = "Payment currency (OrderPayment.currency)", example = "ETB")
    SupportedCurrency currency,

    @Schema(description = "Payment status (OrderPayment.status)", example = "PENDING")
    PaymentStatus status,

    @Schema(description = "Order number (Order.orderNumber)", example = "ORD-20240101-ABC12345")
    String orderNumber,

    @Schema(description = "Customer full name (Customer.firstName + lastName)", example = "John Doe")
    String customerFullName,

    @Schema(description = "Customer email (Credential.emailAddress)", example = "john@example.com")
    String customerEmail,

    @Schema(description = "Customer phone (Credential.phoneNumber)", example = "+251911234567")
    String customerPhone,

    @Schema(description = "Payment created at (OrderPayment.createdAt)", example = "2024-01-15T10:30:00")
    LocalDateTime createdAt,

    @Schema(description = "When payment was initiated (OrderPayment.initRequestedAt)", example = "2024-01-15T10:30:00")
    LocalDateTime initRequestedAt,

    @Schema(description = "When payment was resolved (OrderPayment.resolvedAt)", example = "2024-01-15T10:35:00")
    LocalDateTime resolvedAt
) {

}
