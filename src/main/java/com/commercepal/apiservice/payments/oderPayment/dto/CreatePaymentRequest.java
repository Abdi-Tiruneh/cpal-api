package com.commercepal.apiservice.payments.oderPayment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for creating a payment for an order.
 */
@Schema(
    name = "CreatePaymentRequest",
    description = "Request payload for creating a payment for an order"
)
public record CreatePaymentRequest(
    @Schema(
        description = "Payment provider code identifying the selected payment method item",
        example = "TELEBIRR",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Payment provider code is required")
    String paymentProviderCode,

    @Schema(
        description = "Payment provider variant code for the selected payment method item variant (optional)",
        example = "TELEBIRR_ETB"
    )
    String paymentProviderVariantCode
) {

}
