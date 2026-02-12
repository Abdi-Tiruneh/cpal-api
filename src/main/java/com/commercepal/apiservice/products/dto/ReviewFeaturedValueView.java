package com.commercepal.apiservice.products.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * Featured engagement metric for a review (e.g., like count, helpful count).
 */
@Builder
@Schema(description = "Featured engagement metric for a review")
public record ReviewFeaturedValueView(
    @Schema(description = "Name of the metric", example = "likeCount") String name,

    @Schema(description = "Value of the metric", example = "42") String value) {

}
