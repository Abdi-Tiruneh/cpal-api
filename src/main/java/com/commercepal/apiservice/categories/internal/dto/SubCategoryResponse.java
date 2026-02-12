package com.commercepal.apiservice.categories.internal.dto;

import com.commercepal.apiservice.categories.enums.CategoryStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;

/**
 * SubCategory response DTO.
 */
@Builder
@Schema(name = "SubCategoryResponse", description = "SubCategory information")
public record SubCategoryResponse(
    @Schema(description = "SubCategory ID", example = "1")
    Long id,

    @Schema(description = "SubCategory name", example = "Smartphones")
    String name,

    @Schema(description = "SubCategory slug", example = "smartphones")
    String slug,

    @Schema(description = "SubCategory description", example = "Mobile phones and smartphones")
    String description,

    @Schema(description = "SubCategory image URL", example = "https://example.com/images/smartphones.jpg")
    String imageUrl,

    @Schema(description = "SubCategory status", example = "ACTIVE")
    CategoryStatus status,

    @Schema(description = "Display order", example = "1")
    Integer displayOrder,

    @Schema(description = "Parent category ID", example = "1")
    Long categoryId,

    @Schema(description = "Parent category name", example = "Electronics")
    String categoryName,

    @Schema(description = "Created at timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdAt,

    @Schema(description = "Updated at timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime updatedAt
) {
}
