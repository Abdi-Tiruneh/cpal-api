package com.commercepal.apiservice.products.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Product imagery for list and detail views")
public record ImageView(
    @Schema(description = "Thumbnail image URL (small size)", example = "https://cdn.example.com/thumb.jpg")
    String thumbnail,

    @Schema(description = "Primary image URL (medium/large size)", example = "https://cdn.example.com/main.jpg")
    String main
) {

}

