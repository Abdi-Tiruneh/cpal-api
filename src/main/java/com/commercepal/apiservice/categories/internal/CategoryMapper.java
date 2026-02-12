package com.commercepal.apiservice.categories.internal;

import com.commercepal.apiservice.categories.api.Category;
import com.commercepal.apiservice.categories.api.SubCategory;
import com.commercepal.apiservice.categories.enums.CategoryStatus;
import com.commercepal.apiservice.categories.internal.dto.CategoryRequest;
import com.commercepal.apiservice.categories.internal.dto.CategoryResponse;
import com.commercepal.apiservice.categories.internal.dto.SimpleCategoryResponse;
import com.commercepal.apiservice.categories.internal.dto.SimpleSubCategoryResponse;
import com.commercepal.apiservice.categories.internal.dto.SubCategoryRequest;
import com.commercepal.apiservice.categories.internal.dto.SubCategoryResponse;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Mapper for Category and SubCategory entities and DTOs.
 */
@Component
public class CategoryMapper {

  /**
   * Maps Category entity to CategoryResponse.
   */
  public CategoryResponse toCategoryResponse(Category category) {
    if (category == null) {
      return null;
    }
    return CategoryResponse.builder()
        .id(category.getId())
        .name(category.getName())
        .slug(category.getSlug())
        .code(category.getCode())
        .description(category.getDescription())
        .imageUrl(category.getImageUrl())
        .status(category.getStatus())
        .displayOrder(category.getDisplayOrder())
        .createdAt(category.getCreatedAt())
        .updatedAt(category.getUpdatedAt())
        .build();
  }

  /**
   * Maps Category entity to SimpleCategoryResponse.
   * Includes active subcategories if they are loaded in the category entity.
   */
  public SimpleCategoryResponse toSimpleCategoryResponse(Category category) {
    if (category == null) {
      return null;
    }
    
    // Map active subcategories if they are loaded
    List<SimpleSubCategoryResponse> subCategories = new ArrayList<>();
    if (category.getSubCategories() != null && !category.getSubCategories().isEmpty()) {
      subCategories = category.getSubCategories().stream()
          .filter(subCategory -> subCategory.getStatus() == CategoryStatus.ACTIVE)
          .map(this::toSimpleSubCategoryResponse)
          .toList();
    }
    
    return SimpleCategoryResponse.builder()
        .name(category.getName())
        .slug(category.getSlug())
        .code(category.getCode())
        .description(category.getDescription())
        .imageUrl(category.getImageUrl())
        .displayOrder(category.getDisplayOrder())
        .providerId(category.getProviderId())
        .subCategories(subCategories)
        .build();
  }
  
  /**
   * Maps Category entity to SimpleCategoryResponse with provided subcategories.
   * This overload allows passing subcategories that are fetched separately.
   */
  public SimpleCategoryResponse toSimpleCategoryResponse(Category category, List<SubCategory> subCategories) {
    if (category == null) {
      return null;
    }
    
    // Map active subcategories from the provided list
    List<SimpleSubCategoryResponse> mappedSubCategories = new ArrayList<>();
    if (subCategories != null && !subCategories.isEmpty()) {
      mappedSubCategories = subCategories.stream()
          .filter(subCategory -> subCategory.getStatus() == CategoryStatus.ACTIVE)
          .map(this::toSimpleSubCategoryResponse)
          .toList();
    }
    
    return SimpleCategoryResponse.builder()
        .name(category.getName())
        .slug(category.getSlug())
        .code(category.getCode())
        .description(category.getDescription())
        .imageUrl(category.getImageUrl())
        .displayOrder(category.getDisplayOrder())
        .providerId(category.getProviderId())
        .subCategories(mappedSubCategories)
        .build();
  }

  /**
   * Maps SubCategory entity to SubCategoryResponse.
   */
  public SubCategoryResponse toSubCategoryResponse(SubCategory subCategory) {
    if (subCategory == null) {
      return null;
    }
    return SubCategoryResponse.builder()
        .id(subCategory.getId())
        .name(subCategory.getName())
        .slug(subCategory.getSlug())
        .description(subCategory.getDescription())
        .imageUrl(subCategory.getImageUrl())
        .status(subCategory.getStatus())
        .displayOrder(subCategory.getDisplayOrder())
        .categoryId(subCategory.getCategory() != null ? subCategory.getCategory().getId() : null)
        .categoryName(subCategory.getCategory() != null ? subCategory.getCategory().getName() : null)
        .createdAt(subCategory.getCreatedAt())
        .updatedAt(subCategory.getUpdatedAt())
        .build();
  }

  /**
   * Maps SubCategory entity to SimpleSubCategoryResponse.
   */
  public SimpleSubCategoryResponse toSimpleSubCategoryResponse(SubCategory subCategory) {
    if (subCategory == null) {
      return null;
    }
    return SimpleSubCategoryResponse.builder()
        .name(subCategory.getName())
        .slug(subCategory.getSlug())
        .description(subCategory.getDescription())
        .imageUrl(subCategory.getImageUrl())
        .displayOrder(subCategory.getDisplayOrder())
        .categoryName(subCategory.getCategory() != null ? subCategory.getCategory().getName() : null)
        .providerId(subCategory.getProviderId())
        .build();
  }

  /**
   * Maps CategoryRequest to Category entity.
   * Generates code from name if not provided.
   */
  public Category toCategory(CategoryRequest request) {
    if (request == null) {
      return null;
    }
    String code = request.code();
    if (code == null || code.trim().isEmpty()) {
      code = generateCodeFromName(request.name());
    } else {
      code = code.toUpperCase().trim();
    }
    
    return Category.builder()
        .name(request.name())
        .slug(request.slug())
        .code(code)
        .description(request.description())
        .imageUrl(request.imageUrl())
        .displayOrder(request.displayOrder() != null ? request.displayOrder() : 0)
        .build();
  }

  /**
   * Maps SubCategoryRequest to SubCategory entity.
   */
  public SubCategory toSubCategory(SubCategoryRequest request) {
    if (request == null) {
      return null;
    }
    return SubCategory.builder()
        .name(request.name())
        .slug(request.slug())
        .description(request.description())
        .imageUrl(request.imageUrl())
        .displayOrder(request.displayOrder() != null ? request.displayOrder() : 0)
        .build();
  }

  /**
   * Updates Category entity with values from CategoryRequest.
   */
  public void updateCategoryFromRequest(Category category, CategoryRequest request) {
    if (category == null || request == null) {
      return;
    }
    category.setName(request.name());
    category.setSlug(request.slug());
    if (request.code() != null && !request.code().trim().isEmpty()) {
      category.setCode(request.code().toUpperCase().trim());
    }
    category.setDescription(request.description());
    category.setImageUrl(request.imageUrl());
    if (request.displayOrder() != null) {
      category.setDisplayOrder(request.displayOrder());
    }
  }

  /**
   * Generates a category code from the category name.
   * Takes first letters of each word and converts to uppercase.
   * Example: "Home Appliances" -> "HA", "Electronics" -> "ELEC"
   */
  private String generateCodeFromName(String name) {
    if (name == null || name.trim().isEmpty()) {
      return "";
    }
    String[] words = name.trim().split("\\s+");
    if (words.length == 1) {
      // Single word: take first 4 characters
      String word = words[0].toUpperCase();
      return word.length() > 4 ? word.substring(0, 4) : word;
    } else {
      // Multiple words: take first letter of each word
      StringBuilder code = new StringBuilder();
      for (String word : words) {
        if (!word.isEmpty()) {
          code.append(word.charAt(0));
        }
      }
      return code.toString().toUpperCase();
    }
  }

  /**
   * Updates SubCategory entity with values from SubCategoryRequest.
   */
  public void updateSubCategoryFromRequest(SubCategory subCategory, SubCategoryRequest request) {
    if (subCategory == null || request == null) {
      return;
    }
    subCategory.setName(request.name());
    subCategory.setSlug(request.slug());
    subCategory.setDescription(request.description());
    subCategory.setImageUrl(request.imageUrl());
    if (request.displayOrder() != null) {
      subCategory.setDisplayOrder(request.displayOrder());
    }
  }
}
