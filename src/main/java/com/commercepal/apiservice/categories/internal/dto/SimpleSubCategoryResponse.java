package com.commercepal.apiservice.categories.internal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * Simple subcategory response DTO for customer-facing endpoints.
 */
@Builder
@Schema(description = "Subcategory information")
public record SimpleSubCategoryResponse(
    @Schema(description = "Subcategory name", example = "Smartphones")
    String name,

    @Schema(description = "Subcategory slug", example = "smartphones")
    String slug,

    @Schema(description = "Subcategory description", example = "Mobile phones and smartphones")
    String description,

    @Schema(description = "Subcategory image URL", example = "https://example.com/images/smartphones.jpg")
    String imageUrl,

    @Schema(description = "Display order", example = "1")
    Integer displayOrder,

    @Schema(description = "Parent category name", example = "Electronics")
    String categoryName,

    @Schema(description = "Provider ID", example = "provider-123")
    String providerId
) {
}
