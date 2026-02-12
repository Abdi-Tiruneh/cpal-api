package com.commercepal.apiservice.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request object for text an individual item to add to the cart.
 */
@Schema(name = "CartItemRequest", description = "Individual item details for add to cart request")
public record CartItemRequest(

    @Schema(description = "Product ID to add to cart (required unless productName/price provided)", example = "aesg-1005007345692120") @NotBlank String productId,

    @Schema(description = "Product variant/configuration ID", example = "100887021410", nullable = true) String configId,

    @Schema(description = "Quantity to add", example = "1", minimum = "1") @Min(value = 1, message = "Quantity must be at least 1") @NotNull Integer quantity,

    @Schema(description = "Currency Code", example = "USD") String currency,

    @Schema(description = "Country Code", example = "ET") String country) {

}
