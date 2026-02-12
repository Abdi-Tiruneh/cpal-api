package com.commercepal.apiservice.products.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

/**
 * Comprehensive product detail response with all information needed for product detail page. This
 * DTO provides a complete view of a product including variants, attributes, media, and
 * specifications.
 */
@Builder
@Schema(description = "Complete product detail information for product detail page")
public record ProductDetailResponse(
    // ===== BASIC INFORMATION =====
    @Schema(description = "Unique product identifier", example = "aesg-1005007047740156") String id,

    @Schema(description = "Product title", example = "SL17 Magnetic Phones Cooler") String title,

    @Schema(description = "Provider/marketplace code", example = "AliexpressSingapore") String provider,

    @Schema(description = "Brand name", example = "HTHwish") String brandName,

    @Schema(description = "Vendor/store name", example = "Shop1103608728 Store") String vendorName,

    @Schema(description = "Category ID", example = "aesg-200958006") String categoryId,

    // ===== DESCRIPTION & DETAILS =====
    @Schema(description = "Parsed product description as array of feature points") List<String> description,

    @Schema(description = "Physical dimensions and weight") PhysicalParametersView physicalParameters,

    // ===== AVAILABILITY & STATUS =====
    @Schema(description = "Availability status", example = "AVAILABLE") String status,

    @Schema(description = "Total available stock quantity", example = "99999") Integer stockLevel,

    @Schema(description = "Whether product is allowed to be sold", example = "true") boolean isSellAllowed,

    @Schema(description = "Product condition", example = "New") String stuffStatus,

    // ===== PRICING =====
    @Schema(description = "Product pricing information with discounts") PricingView pricing,

    // ===== MEDIA =====
    @Schema(description = "Product images") List<ImageView> images,

    @Schema(description = "Main/primary product image") ImageView mainImage,

    @Schema(description = "Product videos") List<VideoView> videos,

    // ===== VARIANTS & CONFIGURATIONS
    @Schema(description = "Product variants/SKUs with individual pricing and stock") List<ProductVariantView> variants,

    @Schema(description = "Whether product has hierarchical configurators (nested options)", example = "false") boolean hasHierarchicalConfigurators,

    // ===== EXTERNAL LINKS =====
    @Schema(description = "External product URL", example = "https://www.aliexpress.com/item/1005007047740156.html") String externalUrl,

    // ===== ORDERING INFO =====
    @Schema(description = "Minimum order quantity (first lot quantity)", example = "1") Integer minOrderQuantity,
    @Schema(description = "Quantity increment step (next lot quantity)", example = "1") Integer quantityStep,

    // ===== TIMESTAMPS =====
    @Schema(description = "Product creation timestamp", example = "2024-10-08T10:00:46.06") String createdTime,

    @Schema(description = "Product last update timestamp", example = "2024-11-17T15:03:13.761") String updatedTime,

    // ===== RATINGS & REVIEWS =====
    @Schema(description = "Rating and review metadata") MetaView meta,

    @Schema(description = "Customer reviews with ratings, images, and engagement metrics") List<CustomerReviewView> customerReviews,

    @Schema(description = "Recommended products based on this product") List<ProductCardResponse> recommendedProducts) {

}
