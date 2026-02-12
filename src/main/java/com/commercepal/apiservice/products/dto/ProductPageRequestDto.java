package com.commercepal.apiservice.products.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import org.json.JSONObject;
import org.springdoc.core.annotations.ParameterObject;

/**
 * Immutable record for product search parameters.
 */
@ParameterObject
@Schema(description = "Product search request parameters")
public record ProductPageRequestDto(
    @Schema(description = "Page number (0-based index)", example = "0") @Min(0) Integer page,
    @Schema(description = "Number of records per page", example = "36") @Min(1) Integer size,
    @Schema(description = "Search query", example = "laptop") String query,

    // Filters
    @Schema(description = "Category ID") String categoryId,
    @Schema(description = "Provider") String provider,
    @Schema(description = "Order By") String orderBy,
    @Schema(description = "Brand ID") String brandId,

    // Boolean
    @Schema(description = "Is Tmall") Boolean isTmall,
    @Schema(description = "Use Optimal Frame Size") Boolean useOptimalFrameSize,

    // Long
    @Schema(description = "Max Volume") Long maxVolume,
    @Schema(description = "Min Volume") Long minVolume,

    // BigDecimal
    @Schema(description = "Min Price") BigDecimal minPrice,
    @Schema(description = "Max Price") BigDecimal maxPrice) {

  public JSONObject toJsonObject() {
    JSONObject json = new JSONObject();

    putIfPresent(json, "ItemTitle", query);
    putIfPresent(json, "CategoryId", categoryId);
    putIfPresent(json, "Provider", provider);
    putIfPresent(json, "0rderBy", orderBy);
    putIfPresent(json, "BrandId", brandId);

    if (isTmall != null) {
      json.put("IsTmall", isTmall);
    }
    if (useOptimalFrameSize != null) {
      json.put("UseOptimalFrameSize", useOptimalFrameSize);
    }

    if (maxVolume != null) {
      json.put("MaxVolume", maxVolume);
    }
    if (minVolume != null) {
      json.put("MinVolume", minVolume);
    }

    if (minPrice != null) {
      json.put("MinPrice", minPrice);
    }
    if (maxPrice != null) {
      json.put("MaxPrice", maxPrice);
    }

    return json;
  }

  private void putIfPresent(JSONObject obj, String key, String value) {
    if (value != null && !value.isBlank()) {
      obj.put(key, value);
    }
  }

  public int getPageOrDefault() {
    return page != null && page >= 0 ? page : 0;
  }

  public int getSizeOrDefault() {
    return size != null && size > 0 ? size : 36;
  }

  public ProductPageRequestDto withProvider(String provider) {
    return new ProductPageRequestDto(
        page, size, query, categoryId, provider, orderBy, brandId, isTmall,
        useOptimalFrameSize, maxVolume, minVolume, minPrice, maxPrice);
  }

  public ProductPageRequestDto withCategoryId(String categoryId) {
    return new ProductPageRequestDto(
        page, size, query, categoryId, provider, orderBy, brandId, isTmall,
        useOptimalFrameSize, maxVolume, minVolume, minPrice, maxPrice);
  }

  public ProductPageRequestDto withQuery(String query) {
    return new ProductPageRequestDto(
        page, size, query, categoryId, provider, orderBy, brandId, isTmall,
        useOptimalFrameSize, maxVolume, minVolume, minPrice, maxPrice);
  }

  public ProductPageRequestDto withPageSize(int page, int size) {
    return new ProductPageRequestDto(
        page, size, query, categoryId, provider, orderBy, brandId, isTmall,
        useOptimalFrameSize, maxVolume, minVolume, minPrice, maxPrice);
  }
}
