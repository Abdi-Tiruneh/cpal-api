package com.commercepal.apiservice.products.service;

import com.commercepal.apiservice.products.dto.CustomerReviewView;
import com.commercepal.apiservice.products.dto.ReviewFeaturedValueView;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

/**
 * Professional service for transforming provider review data into CustomerReviewView DTOs. Handles
 * null-safety, data validation, and robust error handling.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewTransformService {

  private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

  /**
   * Transforms provider reviews JSON into a list of CustomerReviewView objects. Handles null values
   * gracefully and validates data integrity.
   *
   * @param providerReviews JSONObject containing "TotalCount" and "Content" array
   * @return List of CustomerReviewView objects, empty list if input is null or invalid
   */
  public List<CustomerReviewView> transformProviderReviews(JSONObject providerReviews) {
    if (providerReviews == null || providerReviews.isEmpty()) {
      log.debug("Provider reviews object is null or empty");
      return Collections.emptyList();
    }

    JSONArray contentArray = providerReviews.optJSONArray("Content");
    if (contentArray == null || contentArray.isEmpty()) {
      log.debug("Reviews content array is null or empty");
      return Collections.emptyList();
    }

    List<CustomerReviewView> reviews = new ArrayList<>();
    for (int i = 0; i < contentArray.length(); i++) {
      JSONObject reviewJson = contentArray.optJSONObject(i);
      if (reviewJson == null) {
        log.warn("Skipping null review at index {}", i);
        continue;
      }

      try {
        CustomerReviewView review = transformSingleReview(reviewJson);
        reviews.add(review);
      } catch (Exception e) {
        log.error("Failed to transform review at index {}: {}", i, e.getMessage(), e);
        // Continue processing other reviews instead of failing completely
      }
    }

    log.info("Successfully transformed {} out of {} reviews", reviews.size(),
        contentArray.length());
    return reviews;
  }

  /**
   * Transforms a single review JSON object into CustomerReviewView. Applies defensive programming
   * with null-safe extraction.
   *
   * @param reviewJson Single review JSON object
   * @return CustomerReviewView with all fields properly mapped
   */
  private CustomerReviewView transformSingleReview(JSONObject reviewJson) {
    return CustomerReviewView.builder()
        .content(extractString(reviewJson, "Content"))
        .rating(extractRating(reviewJson))
        .configId(extractString(reviewJson, "ConfigurationId"))
        .reviewedAt(extractDateTime(reviewJson, "CreatedDate"))
        .images(extractImages(reviewJson))
        .featuredValues(extractFeaturedValues(reviewJson))
        .build();
  }

  /**
   * Safely extracts a string value from JSON object.
   *
   * @param json JSON object
   * @param key  Field key
   * @return String value or null if not present/empty
   */
  private String extractString(JSONObject json, String key) {
    if (json == null || !json.has(key)) {
      return null;
    }
    String value = json.optString(key, null);
    return (value != null && !value.trim().isEmpty()) ? value : null;
  }

  /**
   * Extracts and validates rating value (1-5 stars). Defaults to 0 if invalid or missing.
   *
   * @param json Review JSON object
   * @return Rating value between 0-5
   */
  private int extractRating(JSONObject json) {
    if (json == null || !json.has("Rating")) {
      return 0;
    }
    int rating = json.optInt("Rating", 0);
    // Validate rating is within acceptable range
    return (rating >= 0 && rating <= 5) ? rating : 0;
  }

  /**
   * Parses ISO 8601 datetime string into LocalDateTime. Returns null if parsing fails or value is
   * missing.
   *
   * @param json JSON object
   * @param key  Field key
   * @return LocalDateTime or null if invalid
   */
  private LocalDateTime extractDateTime(JSONObject json, String key) {
    String dateStr = extractString(json, key);
    if (dateStr == null) {
      return null;
    }

    try {
      return LocalDateTime.parse(dateStr, ISO_FORMATTER);
    } catch (DateTimeParseException e) {
      log.warn("Failed to parse date '{}' for key '{}': {}", dateStr, key, e.getMessage());
      return null;
    }
  }

  /**
   * Extracts image URLs from the Images array. Returns empty list if array is null or empty.
   *
   * @param json Review JSON object
   * @return List of image URL strings
   */
  private List<String> extractImages(JSONObject json) {
    if (json == null || !json.has("Images")) {
      return Collections.emptyList();
    }

    JSONArray imagesArray = json.optJSONArray("Images");
    if (imagesArray == null || imagesArray.isEmpty()) {
      return Collections.emptyList();
    }

    List<String> images = new ArrayList<>();
    for (int i = 0; i < imagesArray.length(); i++) {
      String imageUrl = imagesArray.optString(i, null);
      if (imageUrl != null && !imageUrl.trim().isEmpty()) {
        images.add(imageUrl);
      }
    }
    return images;
  }

  /**
   * Extracts featured engagement values (likes, helpful counts, etc.). Returns empty list if array
   * is null or empty.
   *
   * @param json Review JSON object
   * @return List of ReviewFeaturedValueView objects
   */
  private List<ReviewFeaturedValueView> extractFeaturedValues(JSONObject json) {
    if (json == null || !json.has("FeaturedValues")) {
      return Collections.emptyList();
    }

    JSONArray featuredArray = json.optJSONArray("FeaturedValues");
    if (featuredArray == null || featuredArray.isEmpty()) {
      return Collections.emptyList();
    }

    List<ReviewFeaturedValueView> featuredValues = new ArrayList<>();
    for (int i = 0; i < featuredArray.length(); i++) {
      JSONObject featuredJson = featuredArray.optJSONObject(i);
      if (featuredJson == null) {
        continue;
      }

      String name = extractString(featuredJson, "Name");
      String value = extractString(featuredJson, "Value");

      // Only add if both name and value are present
      if (name != null && value != null) {
        featuredValues.add(ReviewFeaturedValueView.builder()
            .name(name)
            .value(value)
            .build());
      }
    }
    return featuredValues;
  }

  /**
   * Extracts total review count from provider reviews object. Returns 0 if not present or invalid.
   *
   * @param providerReviews Provider reviews JSON object
   * @return Total count of reviews
   */
  public int extractTotalCount(JSONObject providerReviews) {
    if (providerReviews == null || !providerReviews.has("TotalCount")) {
      return 0;
    }
    return Math.max(0, providerReviews.optInt("TotalCount", 0));
  }
}
