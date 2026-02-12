package com.commercepal.apiservice.products.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Product video information")
public record VideoView(
    @Schema(description = "Video URL", example = "https://video.example.com/product.mp4") String url,

    @Schema(description = "Video preview/thumbnail URL", example = "https://cdn.example.com/preview.jpg") String previewUrl) {

}
