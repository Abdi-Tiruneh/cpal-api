package com.commercepal.apiservice.orders.checkout.dto;

import com.commercepal.apiservice.orders.enums.PaymentStatus;
import com.commercepal.apiservice.payments.oderPayment.dto.PaymentInitiationResponse;
import com.commercepal.apiservice.shared.enums.Channel;
import com.commercepal.apiservice.shared.enums.SupportedCurrency;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;

/**
 * Professional checkout response DTO containing all necessary data for payment processing. This
 * response provides complete order information needed for payment gateway integration.
 */
@Builder
@Schema(name = "CheckoutResponse", description = "Complete checkout response containing order details and all data required for payment processing")
public record CheckoutResponse(

    @Schema(description = "Human-readable order number for customer reference", example = "ORD-1702407470123-456", requiredMode = Schema.RequiredMode.REQUIRED) String orderNumber,

    @Schema(description = "Platform/channel where the order was placed", example = "WEB", requiredMode = Schema.RequiredMode.REQUIRED) Channel platform,

    @Schema(description = "Currency used for the order", example = "ETB", requiredMode = Schema.RequiredMode.REQUIRED) SupportedCurrency currency,

    @Schema(description = "Order financial summary", requiredMode = Schema.RequiredMode.REQUIRED) FinancialSummary pricingSummary,

    @Schema(description = "Current payment status", example = "PENDING", requiredMode = Schema.RequiredMode.REQUIRED) PaymentStatus paymentStatus,

    @Schema(description = "Timestamp when the order was created", example = "2025-12-12T21:57:50", requiredMode = Schema.RequiredMode.REQUIRED) LocalDateTime orderedAt,

    @Schema(description = "Payment response details for payment processing", requiredMode = Schema.RequiredMode.NOT_REQUIRED) PaymentInitiationResponse paymentInitiation) {

  /**
   * Financial summary for the entire order
   */
  @Builder
  @Schema(description = "Complete financial breakdown of the order")
  public record FinancialSummary(
      @Schema(description = "Total before tax and fees", example = "25000.00") BigDecimal subtotal,

      @Schema(description = "Total discount amount", example = "2500.00") BigDecimal discountAmount,

      @Schema(description = "Total delivery fees", example = "0.00") BigDecimal deliveryFee,

      @Schema(description = "Additional charges if any", example = "0.00") BigDecimal additionalCharges,

      @Schema(description = "Final total amount to be paid", example = "22500.00") BigDecimal totalAmount,

      @Schema(description = "Currency for all amounts", example = "ETB") SupportedCurrency currency) {

  }

}
