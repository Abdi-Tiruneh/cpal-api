package com.commercepal.apiservice.products.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

/**
 * Customer review with rating, content, images, and engagement metrics. Represents a single product
 * review from a customer.
 */
@Builder
@Schema(description = "Customer product review with rating, content, images, and engagement metrics")
public record CustomerReviewView(

    @Schema(description = "Review text content", example = "I love it is so good") String content,

    @Schema(description = "Star rating (1-5)", example = "5", minimum = "1", maximum = "5") int rating,

    @Schema(description = "Product configuration/variant identifier", example = "I32o646ehujb") String configId,

    @Schema(description = "Date and time when the review was created") LocalDateTime reviewedAt,

    @Schema(description = "List of image URLs attached to the review") List<String> images,

    @Schema(description = "Featured engagement metrics (e.g., like count, helpful count)") List<ReviewFeaturedValueView> featuredValues) {

}
