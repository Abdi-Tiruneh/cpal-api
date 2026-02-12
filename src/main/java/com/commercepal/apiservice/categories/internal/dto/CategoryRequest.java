package com.commercepal.apiservice.categories.internal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Category request DTO for creating/updating categories.
 */
@Schema(name = "CategoryRequest", description = "Request to create or update a category")
public record CategoryRequest(
    @Schema(description = "Category name", example = "Electronics", required = true)
    @NotBlank(message = "Category name is required")
    @Size(max = 255, message = "Category name must not exceed 255 characters")
    String name,

    @Schema(description = "Category slug", example = "electronics", required = true)
    @NotBlank(message = "Category slug is required")
    @Size(max = 255, message = "Category slug must not exceed 255 characters")
    String slug,

    @Schema(description = "Category code (auto-generated if not provided)", example = "ELEC")
    @Size(max = 50, message = "Category code must not exceed 50 characters")
    String code,

    @Schema(description = "Category description", example = "Electronic products and gadgets")
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    String description,

    @Schema(description = "Category image URL", example = "https://example.com/images/electronics.jpg")
    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    String imageUrl,

    @Schema(description = "Display order", example = "1")
    Integer displayOrder
) {
}
