package com.commercepal.apiservice.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

/**
 * Request to update cart item quantity or variant.
 */
@Schema(name = "UpdateCartItemRequest", description = "Request to update cart item")
public record UpdateCartItemRequest(

    @Schema(description = "New quantity (null to keep current)", example = "5", minimum = "1", nullable = true) @Min(value = 1, message = "Quantity must be at least 1") Integer quantity,

    @Schema(description = "Replace with new variant/config ID (null to keep current)", example = "100887021411", nullable = true) String replaceConfigId) {

}
