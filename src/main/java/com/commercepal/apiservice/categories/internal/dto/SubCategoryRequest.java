package com.commercepal.apiservice.categories.internal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * SubCategory request DTO for creating/updating subcategories.
 */
@Schema(name = "SubCategoryRequest", description = "Request to create or update a subcategory")
public record SubCategoryRequest(
    @Schema(description = "SubCategory name", example = "Smartphones", required = true)
    @NotBlank(message = "SubCategory name is required")
    @Size(max = 255, message = "SubCategory name must not exceed 255 characters")
    String name,

    @Schema(description = "SubCategory slug", example = "smartphones", required = true)
    @NotBlank(message = "SubCategory slug is required")
    @Size(max = 255, message = "SubCategory slug must not exceed 255 characters")
    String slug,

    @Schema(description = "SubCategory description", example = "Mobile phones and smartphones")
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    String description,

    @Schema(description = "SubCategory image URL", example = "https://example.com/images/smartphones.jpg")
    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    String imageUrl,

    @Schema(description = "Parent category ID", example = "1", required = true)
    @NotNull(message = "Category ID is required")
    Long categoryId,

    @Schema(description = "Display order", example = "1")
    Integer displayOrder
) {
}
