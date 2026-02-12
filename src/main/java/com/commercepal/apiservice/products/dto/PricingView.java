package com.commercepal.apiservice.products.dto;

import com.commercepal.apiservice.shared.enums.SupportedCurrency;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Builder;

/**
 * Consolidated and pre-formatted pricing information.
 */
@Builder
@Schema(description = "Consolidated Pricing Information with full discount details")
public record PricingView(
    // Currency Info
    @Schema(description = "Currency for prices", example = "ETB")
    SupportedCurrency currency,

    // Raw Price Values (for math/analytics)
    @Schema(description = "Current unit price in currency", example = "1899.99")
    BigDecimal currentPrice,
    @Schema(description = "Original unit price before discount", example = "2499.99")
    BigDecimal originalPrice,
    @Schema(description = "Monetary discount amount", example = "600.00")
    BigDecimal discountAmount, // NEW: Raw monetary amount saved

    // Discount Details
    @Schema(description = "Whether a discount is active", example = "true", name = "isOnDiscount")
    boolean isOnDiscount,
    @Schema(description = "Discount percentage as whole number", example = "24")
    Integer discountPercentage,

    @Schema(description = "Ready-to-display discount percentage string, e.g., '-17%'", example = "-17%")
    String formattedDiscountPercentage,

    // Formatted Strings (ready for UI display)
    @Schema(description = "Formatted current price string", example = "ETB 1,899.99")
    String formattedCurrentPrice,
    @Schema(description = "Formatted original price string", example = "ETB 2,499.99")
    String formattedOriginalPrice,
    @Schema(description = "Formatted discount amount string", example = "Save ETB 600.00")
    String formattedDiscountAmount,

    PricingViewProvider pricingViewProvider
) {

}