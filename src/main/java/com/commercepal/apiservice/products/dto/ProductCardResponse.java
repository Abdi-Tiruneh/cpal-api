package com.commercepal.apiservice.products.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * Optimized product card used in listings/search results.
 */
@Builder
@Schema(description = "Product card used in listings and search results")
public record ProductCardResponse(
    @Schema(description = "Unique product identifier", example = "OT-123456")
    String id,

    @Schema(description = "Display title", example = "Wireless Noise Cancelling Headphones")
    String title,

    @Schema(description = "Provider or marketplace code", example = "SHEIN")
    String provider,

    @Schema(description = "Availability status", example = "AVAILABLE")
    String status,

    @Schema(description = "Available stock units", example = "42")
    Integer stockLevel,

    @Schema(description = "Pricing block with formatted values")
    PricingView pricing,

    @Schema(description = "Primary and thumbnail images")
    ImageView images,

    @Schema(description = "Rating and reviews metadata")
    MetaView meta
) {

}