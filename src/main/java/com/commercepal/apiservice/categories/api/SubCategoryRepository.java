package com.commercepal.apiservice.categories.api;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.commercepal.apiservice.categories.enums.CategoryStatus;

/**
 * Repository for SubCategory entity.
 */
@Repository
public interface SubCategoryRepository extends JpaRepository<SubCategory, Long> {

  /**
   * Find subcategory by slug
   */
  Optional<SubCategory> findBySlug(String slug);

  /**
   * Find all subcategories by category ID
   */
  List<SubCategory> findByCategoryId(Long categoryId);

  /**
   * Find all active subcategories by category ID
   */
  List<SubCategory> findByCategoryIdAndStatusOrderByDisplayOrderAsc(Long categoryId, CategoryStatus status);

  /**
   * Find all subcategories by status with pagination
   */
  Page<SubCategory> findByStatus(CategoryStatus status, Pageable pageable);

  /**
   * Find all subcategories by category ID and status with pagination
   */
  Page<SubCategory> findByCategoryIdAndStatus(Long categoryId, CategoryStatus status, Pageable pageable);

  /**
   * Find all subcategories (regardless of status) with pagination
   */
  Page<SubCategory> findAll(Pageable pageable);

  /**
   * Find all subcategories by category ID (regardless of status) with pagination
   */
  Page<SubCategory> findByCategoryId(Long categoryId, Pageable pageable);

  /**
   * Search subcategories by name (case-insensitive)
   */
  @Query("SELECT sc FROM SubCategory sc WHERE LOWER(sc.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
  Page<SubCategory> searchByName(@Param("searchTerm") String searchTerm, Pageable pageable);

  /**
   * Search subcategories by name and category ID (case-insensitive)
   */
  @Query("SELECT sc FROM SubCategory sc WHERE sc.category.id = :categoryId AND LOWER(sc.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
  Page<SubCategory> searchByNameAndCategoryId(@Param("categoryId") Long categoryId, @Param("searchTerm") String searchTerm, Pageable pageable);
}
