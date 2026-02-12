package com.commercepal.apiservice.payments.oderPayment.dto;

import com.commercepal.apiservice.payments.oderPayment.enums.NextAction;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

/**
 * Payment response information for checkout
 */
@Builder
@Schema(description = "Payment response containing payment gateway details and instructions")
public record PaymentInitiationResponse(
    @Schema(description = "Indicates if the payment initiation was successful", example = "true") boolean success,

    @Schema(description = "Order number associated with this payment transaction", example = "ORD-20240101-ABC12345") String orderNumber,

    @Schema(description = "Unique payment reference/transaction ID from the payment gateway for tracking this payment", example = "TXN-123456789") String paymentReference,

    @Schema(description = "Payment provider code identifying the selected payment method item.", example = "TELEBIRR", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank(message = "Payment provider code is required") String paymentProviderCode,

    @Schema(description = "Payment URL for redirecting customer to payment page", example = "https://payment.gateway.com/pay?ref=TXN-123456789") String paymentUrl,

    @Schema(description = "Payment instructions for the customer", example = "You will be redirected to complete your payment") String paymentInstructions,

    @Schema(description = "Next action required for payment processing", example = "REDIRECT_TO_PAYMENT_URL") NextAction nextAction) {

}
