package com.commercepal.apiservice.categories.internal.dto;

import com.commercepal.apiservice.categories.enums.CategoryStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;

/**
 * Category response DTO.
 */
@Builder
@Schema(name = "CategoryResponse", description = "Category information")
public record CategoryResponse(
    @Schema(description = "Category ID", example = "1")
    Long id,

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

    @Schema(description = "Category status", example = "ACTIVE")
    CategoryStatus status,

    @Schema(description = "Display order", example = "1")
    Integer displayOrder,

    @Schema(description = "Created at timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdAt,

    @Schema(description = "Updated at timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime updatedAt
) {
}
