package com.commercepal.apiservice.payments.paymentMethod.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

/**
 * Response DTO for PaymentMethodItem.
 * <p>
 * Represents a specific payment method item within a payment method category. Contains payment
 * options and configurations with their variants.
 */
@Builder
@Schema(
    name = "PaymentMethodItemResponse",
    description = "Payment method item response containing payment options and their variants"
)
public record PaymentMethodItemResponse(
    @Schema(
        description = "Human-readable display name for the payment method item",
        example = "Telebirr"
    )
    String displayName,

    @Schema(
        description = "Unique code identifying the payment method item",
        example = "TELEBIRR"
    )
    String itemCode,

    @Schema(
        description = "Currency code supported by this payment method item",
        example = "ETB"
    )
    String currency,

    @Schema(
        description = "URL to the icon image for this payment method item",
        example = "https://example.com/icons/telebirr.svg"
    )
    String iconUrl,

    @Schema(
        description = "Instructions for customers on how to complete payment using this method",
        example = "Send money to 0911XXXXXX via Telebirr"
    )
    String paymentInstruction,

    @Schema(
        description = "List of variants available for this payment method item"
    )
    List<PaymentMethodItemVariantResponse> paymentMethodItemResponses
) {

}
