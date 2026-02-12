package com.commercepal.apiservice.payments.paymentMethod.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

/**
 * Response DTO for PaymentMethod.
 * <p>
 * Represents a payment method category with its associated payment items and variants. This is the
 * top-level structure in the payment method hierarchy.
 */
@Builder
@Schema(
    name = "PaymentMethodResponse",
    description = "Payment method category response containing display information and associated payment items"
)
public record PaymentMethodResponse(
    @Schema(
        description = "Human-readable display name for the payment method category",
        example = "Mobile Money"
    )
    String displayName,

    @Schema(
        description = "Unique code identifying the payment method category",
        example = "MOBILE_MONEY"
    )
    String code,

    @Schema(
        description = "URL to the icon image for this payment method category",
        example = "https://example.com/icons/mobile-money.svg"
    )
    String iconUrl,

    @Schema(
        description = "List of payment method items available under this payment method category"
    )
    List<PaymentMethodItemResponse> paymentMethodItemResponses
) {

}
