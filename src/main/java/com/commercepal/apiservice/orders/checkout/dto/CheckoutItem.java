package com.commercepal.apiservice.orders.checkout.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Represents a single item being purchased in the checkout process. Each item contains the product
 * identifier, optional configuration, and quantity.
 */
@Schema(name = "CheckoutItem", description = "Individual item in a checkout request with product ID, optional variant configuration, and quantity")
public record CheckoutItem(

    @Schema(description = "Unique identifier of the product being purchased from the provider", example = "sh-15782573", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank(message = "Item ID is required") String itemId,

    @Schema(description = "Selected configuration or variation ID. Required only if the product has variants (e.g., size, color). Can be null for simple products without variations.", example = "I32o646ewljm", nullable = true) String configId,

    @Schema(description = "Quantity of the item being ordered. Must be at least 1 and respect product-specific minimum order quantities and quantity steps.", example = "10", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "Quantity is required") @Min(value = 1, message = "Quantity must be at least 1") Integer quantity) {

}
