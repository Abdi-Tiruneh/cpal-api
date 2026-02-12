package com.commercepal.apiservice.payments.oderPayment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

/**
 * Request DTO for retrying a payment transaction
 */
@Builder
@Schema(description = "Request to retry a payment transaction with optional payment provider details")
public record PaymentRetryRequest(
    @Schema(
        description = "Unique payment reference/transaction ID from the payment gateway for tracking this payment",
        example = "CP-123456789")
    @NotBlank(message = "Payment reference is required")
    String paymentReference,

    @Schema(
        description = "Payment provider code identifying the selected payment method item (optional)",
        example = "TELEBIRR"
    )
    String paymentProviderCode,

    @Schema(
        description = "Payment provider variant code for the selected payment method item variant (optional)",
        example = "TELEBIRR_ETB"
    )
    String paymentProviderVariantCode
) {

}
