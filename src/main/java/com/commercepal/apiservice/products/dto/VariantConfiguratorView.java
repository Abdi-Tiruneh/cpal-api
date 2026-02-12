package com.commercepal.apiservice.products.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Variant configurator selection with complete attribute details (e.g., Color: Black)")
public record VariantConfiguratorView(
    // IDs for reference
    @Schema(description = "Property ID", example = "14") String propertyId,

    @Schema(description = "Value ID (Vid from attributes)", example = "691") String valueId,

    // Display Information (enriched from Attributes)
    @Schema(description = "Property name (e.g., 'Color', 'Size')", example = "Color") String propertyName,

    @Schema(description = "Display value (e.g., 'SL17-Black Ordinary')", example = "SL17-Black Ordinary") String value,

    // Visual Assets
    @Schema(description = "Full-size image URL for this selection", example = "https://ae01.alicdn.com/kf/black.jpg_.webp") String imageUrl,

    @Schema(description = "Thumbnail/mini image URL for this selection", example = "https://ae01.alicdn.com/kf/black.jpg_100x100.jpg") String miniImageUrl,

    // Metadata
    @Schema(description = "Whether this is a configurator attribute", example = "true") boolean isConfigurator) {

}
