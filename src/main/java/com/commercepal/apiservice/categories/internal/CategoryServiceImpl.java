package com.commercepal.apiservice.categories.internal;

import com.commercepal.apiservice.categories.api.Category;
import com.commercepal.apiservice.categories.api.CategoryRepository;
import com.commercepal.apiservice.categories.api.CategoryService;
import com.commercepal.apiservice.categories.enums.CategoryStatus;
import com.commercepal.apiservice.shared.exceptions.resource.ResourceNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for category operations.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

  private final CategoryRepository categoryRepository;

  @Override
  public List<Category> getAllActiveCategories() {
    return categoryRepository.findByStatusOrderByDisplayOrderAsc(CategoryStatus.ACTIVE);
  }

  @Override
  public Page<Category> getAllCategories(Pageable pageable) {
    return categoryRepository.findAll(pageable);
  }

  @Override
  public Category getCategoryById(Long id) {
    if (id == null) {
      throw new IllegalArgumentException("Category ID cannot be null");
    }
    return categoryRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
  }

  @Override
  public Category getCategoryBySlug(String slug) {
    if (slug == null || slug.trim().isEmpty()) {
      throw new ResourceNotFoundException("Category slug cannot be null or empty");
    }
    return categoryRepository.findBySlug(slug.trim())
        .orElseThrow(() -> new ResourceNotFoundException("Category not found with slug: " + slug));
  }

  @Override
  public Category getCategoryByCode(String code) {
    if (code == null || code.trim().isEmpty()) {
      throw new ResourceNotFoundException("Category code cannot be null or empty");
    }
    return categoryRepository.findByCode(code.trim().toUpperCase())
        .orElseThrow(() -> new ResourceNotFoundException("Category not found with code: " + code));
  }

  /**
   * Get active category by ID (for customers).
   */
  public Category getActiveCategoryById(Long id) {
    if (id == null) {
      throw new IllegalArgumentException("Category ID cannot be null");
    }
    Category category = categoryRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
    if (category.getStatus() != CategoryStatus.ACTIVE) {
      throw new ResourceNotFoundException("Category not found with ID: " + id);
    }
    return category;
  }

  /**
   * Get active category by slug (for customers).
   */
  public Category getActiveCategoryBySlug(String slug) {
    if (slug == null || slug.trim().isEmpty()) {
      throw new ResourceNotFoundException("Category slug cannot be null or empty");
    }
    Category category = categoryRepository.findBySlug(slug.trim())
        .orElseThrow(() -> new ResourceNotFoundException("Category not found with slug: " + slug));
    if (category.getStatus() != CategoryStatus.ACTIVE) {
      throw new ResourceNotFoundException("Category not found with slug: " + slug);
    }
    return category;
  }

  /**
   * Get active category by code (for customers).
   */
  @Override
  public Category getActiveCategoryByCode(String code) {
    if (code == null || code.trim().isEmpty()) {
      throw new ResourceNotFoundException("Category code cannot be null or empty");
    }
    Category category = categoryRepository.findByCode(code.trim().toUpperCase())
        .orElseThrow(() -> new ResourceNotFoundException("Category not found with code: " + code));
    if (category.getStatus() != CategoryStatus.ACTIVE) {
      throw new ResourceNotFoundException("Category not found with code: " + code);
    }
    return category;
  }
}
