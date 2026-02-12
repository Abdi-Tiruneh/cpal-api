package com.commercepal.apiservice.categories.api;

import com.commercepal.apiservice.categories.enums.CategoryStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for Category entity.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

  /**
   * Find category by slug
   */
  Optional<Category> findBySlug(String slug);

  /**
   * Find category by code
   */
  Optional<Category> findByCode(String code);

  /**
   * Check if category slug exists
   */
  boolean existsBySlug(String slug);

  /**
   * Check if category code exists
   */
  boolean existsByCode(String code);

  /**
   * Find all active categories
   */
  List<Category> findByStatusOrderByDisplayOrderAsc(CategoryStatus status);

  /**
   * Find all categories by status with pagination
   */
  Page<Category> findByStatus(CategoryStatus status, Pageable pageable);

  /**
   * Find all categories (regardless of status) with pagination
   */
  Page<Category> findAll(Pageable pageable);

  /**
   * Search categories by name (case-insensitive)
   */
  @Query("SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
  Page<Category> searchByName(@Param("searchTerm") String searchTerm, Pageable pageable);
}
