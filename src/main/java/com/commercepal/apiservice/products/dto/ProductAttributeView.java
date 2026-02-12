package com.commercepal.apiservice.products.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Product attribute/specification")
public record ProductAttributeView(
    @Schema(description = "Attribute property name", example = "Color") String propertyName,

    @Schema(description = "Attribute value", example = "Black") String value,

    @Schema(description = "Whether this is a configurator (variant selector)", example = "true") boolean isConfigurator,

    @Schema(description = "Image URL for this attribute value (if applicable)", example = "https://cdn.example.com/color-black.jpg") String imageUrl,

    @Schema(description = "Mini/thumbnail image URL for this attribute value", example = "https://cdn.example.com/color-black-thumb.jpg") String miniImageUrl) {

}
