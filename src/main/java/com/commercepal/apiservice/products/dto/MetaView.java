package com.commercepal.apiservice.products.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Product popularity metadata")
public record MetaView(
    @Schema(description = "Average rating out of 5", example = "4.6")
    Double rating,

    @Schema(description = "Total number of reviews", example = "128")
    Integer reviewCount
) {

}
