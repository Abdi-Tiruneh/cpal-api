package com.commercepal.apiservice.products.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Builder;

/**
 * Provider pricing information including currency, prices, and markup details.
 */
@Builder
@Schema(description = "Provider Pricing Information with markup details")
public record PricingViewProvider(
    // Provider Currency
    @Schema(description = "Currency code from provider", example = "USD", name = "providerCurrency")
    String providerCurrency,

    // Provider Price Values
    @Schema(description = "Unit price from provider", example = "15.99", name = "providerUnitPrice")
    BigDecimal providerUnitPrice,

    // Markup Information
    /**
     * Markup amount per unit (selling price - cost price)
     */
    @Schema(description = "Markup amount per unit", example = "5.00", name = "unitMarkup")
    BigDecimal unitMarkup
) {

}
