package com.commercepal.apiservice.payments.paymentMethod.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * Response DTO for PaymentMethodItemVariant.
 * <p>
 * Represents a variant of a payment method item, providing specific payment options with different
 * configurations (e.g., different currencies or payment types).
 */
@Builder
@Schema(
    name = "PaymentMethodItemVariantResponse",
    description = "Payment method item variant response containing specific payment option details"
)
public record PaymentMethodItemVariantResponse(
    @Schema(
        description = "Human-readable display name for the payment method item variant",
        example = "Telebirr - ETB"
    )
    String displayName,

    @Schema(
        description = "Unique code identifying the payment method item variant",
        example = "TELEBIRR_ETB"
    )
    String variantCode,

    @Schema(
        description = "Currency code for this specific variant",
        example = "ETB"
    )
    String currency,

    @Schema(
        description = "URL to the icon image for this payment method item variant",
        example = "https://example.com/icons/telebirr-etb.svg"
    )
    String iconUrl,

    @Schema(
        description = "Specific instructions for customers on how to complete payment using this variant",
        example = "Send ETB to account 0911XXXXXX via Telebirr app"
    )
    String paymentInstruction
) {

}
