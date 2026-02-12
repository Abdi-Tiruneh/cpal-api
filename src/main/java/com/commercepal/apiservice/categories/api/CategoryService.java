package com.commercepal.apiservice.categories.api;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for category operations.
 */
public interface CategoryService {

  /**
   * Get all active categories (for customers).
   */
  List<Category> getAllActiveCategories();

  /**
   * Get all categories with pagination (for admin).
   */
  Page<Category> getAllCategories(Pageable pageable);

  /**
   * Get category by ID.
   */
  Category getCategoryById(Long id);

  /**
   * Get category by slug.
   */
  Category getCategoryBySlug(String slug);

  /**
   * Get category by code.
   */
  Category getCategoryByCode(String code);

  /**
   * Get active category by ID (for customers).
   */
  Category getActiveCategoryById(Long id);

  /**
   * Get active category by slug (for customers).
   */
  Category getActiveCategoryBySlug(String slug);

  /**
   * Get active category by code (for customers).
   */
  Category getActiveCategoryByCode(String code);
}
