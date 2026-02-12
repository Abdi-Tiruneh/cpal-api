package com.commercepal.apiservice.products.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Physical dimensions and weight of the product")
public record PhysicalParametersView(
    @Schema(description = "Length in centimeters", example = "9.0") Double length,

    @Schema(description = "Width in centimeters", example = "8.0") Double width,

    @Schema(description = "Height in centimeters", example = "4.0") Double height,

    @Schema(description = "Weight in kilograms", example = "0.112") Double weight) {

}
