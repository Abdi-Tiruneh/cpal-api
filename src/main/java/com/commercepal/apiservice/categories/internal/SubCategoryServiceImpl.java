package com.commercepal.apiservice.categories.internal;

import com.commercepal.apiservice.categories.api.SubCategory;
import com.commercepal.apiservice.categories.api.SubCategoryRepository;
import com.commercepal.apiservice.categories.api.SubCategoryService;
import com.commercepal.apiservice.categories.enums.CategoryStatus;
import com.commercepal.apiservice.shared.exceptions.resource.ResourceNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for subcategory operations.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubCategoryServiceImpl implements SubCategoryService {

  private final SubCategoryRepository subCategoryRepository;

  @Override
  public List<SubCategory> getActiveSubCategoriesByCategoryId(Long categoryId) {
    if (categoryId == null) {
      throw new IllegalArgumentException("Category ID cannot be null");
    }
    return subCategoryRepository
        .findByCategoryIdAndStatusOrderByDisplayOrderAsc(categoryId, CategoryStatus.ACTIVE);
  }

  @Override
  public Page<SubCategory> getAllSubCategories(Pageable pageable) {
    return subCategoryRepository.findAll(pageable);
  }

  @Override
  public Page<SubCategory> getSubCategoriesByCategoryId(Long categoryId, Pageable pageable) {
    if (categoryId == null) {
      throw new IllegalArgumentException("Category ID cannot be null");
    }
    return subCategoryRepository.findByCategoryId(categoryId, pageable);
  }

  @Override
  public SubCategory getSubCategoryById(Long id) {
    if (id == null) {
      throw new IllegalArgumentException("SubCategory ID cannot be null");
    }
    return subCategoryRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("SubCategory not found with ID: " + id));
  }

  @Override
  public SubCategory getSubCategoryBySlug(String slug) {
    if (slug == null || slug.trim().isEmpty()) {
      throw new ResourceNotFoundException("SubCategory slug cannot be null or empty");
    }
    return subCategoryRepository.findBySlug(slug.trim())
        .orElseThrow(() -> new ResourceNotFoundException("SubCategory not found with slug: " + slug));
  }

  /**
   * Get active subcategory by ID (for customers).
   */
  public SubCategory getActiveSubCategoryById(Long id) {
    if (id == null) {
      throw new IllegalArgumentException("SubCategory ID cannot be null");
    }
    SubCategory subCategory = subCategoryRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("SubCategory not found with ID: " + id));
    if (subCategory.getStatus() != CategoryStatus.ACTIVE) {
      throw new ResourceNotFoundException("SubCategory not found with ID: " + id);
    }
    return subCategory;
  }

  /**
   * Get active subcategory by slug (for customers).
   */
  public SubCategory getActiveSubCategoryBySlug(String slug) {
    if (slug == null || slug.trim().isEmpty()) {
      throw new ResourceNotFoundException("SubCategory slug cannot be null or empty");
    }
    SubCategory subCategory = subCategoryRepository.findBySlug(slug.trim())
        .orElseThrow(() -> new ResourceNotFoundException("SubCategory not found with slug: " + slug));
    if (subCategory.getStatus() != CategoryStatus.ACTIVE) {
      throw new ResourceNotFoundException("SubCategory not found with slug: " + slug);
    }
    return subCategory;
  }
}
