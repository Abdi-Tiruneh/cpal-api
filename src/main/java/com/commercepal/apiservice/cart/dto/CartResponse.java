package com.commercepal.apiservice.cart.dto;

import com.commercepal.apiservice.shared.enums.SupportedCurrency;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

/**
 * Complete shopping cart response DTO.
 */
@Builder
@Schema(name = "CartResponse", description = "Complete shopping cart with all details")
public record CartResponse(

    @Schema(description = "Cart ID") Long cartId,

    @Schema(description = "Total number of items") Integer totalItems,

    @Schema(description = "Subtotal amount") BigDecimal subtotal,

    @Schema(description = "Estimated total") BigDecimal estimatedTotal,

    @Schema(description = "Currency") SupportedCurrency currency,

    @Schema(description = "Last activity timestamp") LocalDateTime lastActivityAt,

    @Schema(description = "List of cart items") List<CartItemResponse> items,

    @Schema(description = "Items with price drops") List<CartItemResponse> priceDropItems,

    @Schema(description = "Unavailable items") List<CartItemResponse> unavailableItems,

    @Schema(description = "Total savings from price drops") BigDecimal totalSavings) {

}
