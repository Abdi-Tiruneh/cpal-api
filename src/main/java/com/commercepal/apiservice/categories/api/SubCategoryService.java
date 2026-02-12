package com.commercepal.apiservice.categories.api;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for subcategory operations.
 */
public interface SubCategoryService {

  /**
   * Get all active subcategories by category ID (for customers).
   */
  List<SubCategory> getActiveSubCategoriesByCategoryId(Long categoryId);

  /**
   * Get all subcategories with pagination (for admin).
   */
  Page<SubCategory> getAllSubCategories(Pageable pageable);

  /**
   * Get all subcategories by category ID with pagination (for admin).
   */
  Page<SubCategory> getSubCategoriesByCategoryId(Long categoryId, Pageable pageable);

  /**
   * Get subcategory by ID.
   */
  SubCategory getSubCategoryById(Long id);

  /**
   * Get subcategory by slug.
   */
  SubCategory getSubCategoryBySlug(String slug);

  /**
   * Get active subcategory by ID (for customers).
   */
  SubCategory getActiveSubCategoryById(Long id);

  /**
   * Get active subcategory by slug (for customers).
   */
  SubCategory getActiveSubCategoryBySlug(String slug);
}
