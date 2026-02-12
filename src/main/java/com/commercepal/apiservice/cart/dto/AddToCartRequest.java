package com.commercepal.apiservice.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import java.util.List;

/**
 * Request to add an item to shopping cart.
 */
@Schema(name = "AddToCartRequest", description = "Request to add multiple items to shopping cart")
public record AddToCartRequest(
    @Schema(description = "List of items to add") @Valid List<CartItemRequest> items) {

}
