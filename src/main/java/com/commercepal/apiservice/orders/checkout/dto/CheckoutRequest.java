package com.commercepal.apiservice.orders.checkout.dto;

import com.commercepal.apiservice.shared.enums.Channel;
import com.commercepal.apiservice.shared.enums.SupportedCurrency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Request payload for initiating a checkout operation. Contains all necessary information to create
 * an order including channel, currency, delivery address, and items.
 */
@Schema(name = "CheckoutRequest", description = "Complete checkout request payload containing channel, currency, delivery address, and list of items to purchase")
public record CheckoutRequest(

    @Schema(description = "The platform/channel where the checkout request originates (e.g., WEB, MOBILE_APP, MOBILE_WEB)", example = "WEB", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "Channel is required") Channel channel,

    @Schema(description = "Currency to be used for the checkout transaction. Determines pricing and payment processing.", example = "ETB", allowableValues = {
        "ETB", "USD", "SOS", "KES",
        "AED"}, requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "Currency is required") SupportedCurrency currency,

    @Schema(description = "ID of the customer's delivery address where the order will be shipped. Must belong to the authenticated customer.", example = "12345", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "Delivery address ID is required") Long deliveryAddressId,

    @Schema(description = "List of items to be purchased in this checkout. Must contain at least one item.", requiredMode = Schema.RequiredMode.REQUIRED) @NotEmpty(message = "At least one item is required for checkout") @Valid List<CheckoutItem> items,

    @Schema(description = "Payment provider code identifying the selected payment method item.", example = "TELEBIRR", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank(message = "Payment provider code is required") String paymentProviderCode,

    @Schema(description = "Payment provider variant code for the selected payment method item variant (optional).", example = "TELEBIRR_ETB") String paymentProviderVariantCode,

    @Schema(description = "Payment account identifier for the payment method. Typically a phone number, but can also be an email address depending on the payment provider.", example = "+251911234567") String paymentAccount,

    @Schema(description = "Promotional code to apply discounts or special offers to the order (optional).", example = "SAVE20") String promoCode,

    @Schema(description = "Referral code for tracking referral sources and applying referral benefits (optional).", example = "REF123456") String referralCode) {

}

