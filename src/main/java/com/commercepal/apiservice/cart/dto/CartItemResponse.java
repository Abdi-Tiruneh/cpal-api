package com.commercepal.apiservice.cart.dto;

import com.commercepal.apiservice.shared.enums.SupportedCurrency;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Builder;

/**
 * Individual cart item response DTO.
 */
@Builder
@Schema(name = "CartItemResponse", description = "Individual cart item details")
public record CartItemResponse(

    @Schema(description = "Cart item ID") Long id,

    @Schema(description = "Product ID") String productId,

    @Schema(description = "Product name") String productName,

    @Schema(description = "Product image URL") String productImageUrl,

    @Schema(description = "Quantity") Integer quantity,

    @Schema(description = "Unit price") BigDecimal unitPrice,

    @Schema(description = "Subtotal") BigDecimal subtotal,

    @Schema(description = "Currency") SupportedCurrency currency,

    @Schema(description = "Provider") String provider,

    @Schema(description = "Stock status") String stockStatus,

    @Schema(description = "Is available for purchase") Boolean isAvailable,

    @Schema(description = "Price when added") BigDecimal priceWhenAdded,

    @Schema(description = "Current price") BigDecimal currentPrice,

    @Schema(description = "Has price dropped") Boolean priceDropped,

    @Schema(description = "Savings amount if price dropped") BigDecimal savingsAmount) {

}
