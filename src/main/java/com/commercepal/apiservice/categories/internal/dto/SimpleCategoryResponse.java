package com.commercepal.apiservice.categories.internal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

/**
 * Simple category response DTO for customer-facing endpoints.
 */
@Builder
@Schema(description = "Category information")
public record SimpleCategoryResponse(
    @Schema(description = "Category name", example = "Electronics")
    String name,

    @Schema(description = "Category slug", example = "electronics")
    String slug,

    @Schema(description = "Category code", example = "ELEC")
    String code,

    @Schema(description = "Category description", example = "Electronic products and gadgets")
    String description,

    @Schema(description = "Category image URL", example = "https://example.com/images/electronics.jpg")
    String imageUrl,

    @Schema(description = "Display order", example = "1")
    Integer displayOrder,

    @Schema(description = "Provider ID", example = "provider-123")
    String providerId,

    @Schema(description = "List of active subcategories", example = "[]")
    List<SimpleSubCategoryResponse> subCategories
) {
}
