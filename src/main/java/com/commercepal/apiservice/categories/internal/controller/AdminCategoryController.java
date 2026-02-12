package com.commercepal.apiservice.categories.internal.controller;

import com.commercepal.apiservice.categories.api.CategoryService;
import com.commercepal.apiservice.categories.api.SubCategoryService;
import com.commercepal.apiservice.categories.internal.CategoryMapper;
import com.commercepal.apiservice.categories.internal.dto.CategoryResponse;
import com.commercepal.apiservice.categories.internal.dto.SubCategoryResponse;
import com.commercepal.apiservice.utils.response.PagedResponse;
import com.commercepal.apiservice.utils.response.ResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin REST controller for category and subcategory management operations.
 */
@RestController
@RequiredArgsConstructor
//@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Category Management (Admin)", description = "Admin APIs for managing categories and subcategories")
public class AdminCategoryController {

  private final CategoryService categoryService;
  private final SubCategoryService subCategoryService;
  private final CategoryMapper categoryMapper;

  // Category endpoints
  @GetMapping(value = "/api/v1/admin/categories", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get all categories")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Success"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Forbidden")
  })
  public ResponseEntity<ResponseWrapper<PagedResponse<CategoryResponse>>> getAllCategories(
      @Parameter(description = "Page number (0-based)", example = "0")
      @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size", example = "20")
      @RequestParam(defaultValue = "20") int size,
      @Parameter(description = "Sort field", example = "displayOrder")
      @RequestParam(defaultValue = "displayOrder") String sortBy,
      @Parameter(description = "Sort direction", example = "ASC")
      @RequestParam(defaultValue = "ASC") String sortDir) {
    Sort sort = sortDir.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
    Pageable pageable = PageRequest.of(page, size, sort);
    Page<CategoryResponse> categoriesPage = categoryService.getAllCategories(pageable)
        .map(categoryMapper::toCategoryResponse);
    return ResponseWrapper.success(categoriesPage);
  }

  @GetMapping(value = "/api/v1/admin/categories/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get category by ID")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Success"),
      @ApiResponse(responseCode = "404", description = "Not found"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Forbidden")
  })
  public ResponseEntity<ResponseWrapper<CategoryResponse>> getCategoryById(
      @Parameter(description = "Category ID", example = "1", required = true)
      @PathVariable Long id) {
    CategoryResponse category = categoryMapper.toCategoryResponse(categoryService.getCategoryById(id));
    return ResponseWrapper.success(category);
  }

  @GetMapping(value = "/api/v1/admin/categories/slug/{slug}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get category by slug")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Success"),
      @ApiResponse(responseCode = "404", description = "Not found"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Forbidden")
  })
  public ResponseEntity<ResponseWrapper<CategoryResponse>> getCategoryBySlug(
      @Parameter(description = "Category slug", example = "electronics", required = true)
      @PathVariable String slug) {
    CategoryResponse category = categoryMapper.toCategoryResponse(categoryService.getCategoryBySlug(slug));
    return ResponseWrapper.success(category);
  }

  // Subcategory endpoints
  @GetMapping(value = "/api/v1/admin/subcategories", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get all subcategories")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Success"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Forbidden")
  })
  public ResponseEntity<ResponseWrapper<PagedResponse<SubCategoryResponse>>> getAllSubCategories(
      @Parameter(description = "Page number (0-based)", example = "0")
      @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size", example = "20")
      @RequestParam(defaultValue = "20") int size,
      @Parameter(description = "Sort field", example = "displayOrder")
      @RequestParam(defaultValue = "displayOrder") String sortBy,
      @Parameter(description = "Sort direction", example = "ASC")
      @RequestParam(defaultValue = "ASC") String sortDir) {
    Sort sort = sortDir.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
    Pageable pageable = PageRequest.of(page, size, sort);
    Page<SubCategoryResponse> subCategoriesPage = subCategoryService.getAllSubCategories(pageable)
        .map(categoryMapper::toSubCategoryResponse);
    return ResponseWrapper.success(subCategoriesPage);
  }

  @GetMapping(value = "/api/v1/admin/subcategories/category/{categoryId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get subcategories by category ID")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Success"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Forbidden")
  })
  public ResponseEntity<ResponseWrapper<PagedResponse<SubCategoryResponse>>> getSubCategoriesByCategoryId(
      @Parameter(description = "Category ID", example = "1", required = true)
      @PathVariable Long categoryId,
      @Parameter(description = "Page number (0-based)", example = "0")
      @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size", example = "20")
      @RequestParam(defaultValue = "20") int size,
      @Parameter(description = "Sort field", example = "displayOrder")
      @RequestParam(defaultValue = "displayOrder") String sortBy,
      @Parameter(description = "Sort direction", example = "ASC")
      @RequestParam(defaultValue = "ASC") String sortDir) {
    Sort sort = sortDir.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
    Pageable pageable = PageRequest.of(page, size, sort);
    Page<SubCategoryResponse> subCategoriesPage = subCategoryService.getSubCategoriesByCategoryId(categoryId, pageable)
        .map(categoryMapper::toSubCategoryResponse);
    return ResponseWrapper.success(subCategoriesPage);
  }

  @GetMapping(value = "/api/v1/admin/subcategories/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get subcategory by ID")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Success"),
      @ApiResponse(responseCode = "404", description = "Not found"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Forbidden")
  })
  public ResponseEntity<ResponseWrapper<SubCategoryResponse>> getSubCategoryById(
      @Parameter(description = "SubCategory ID", example = "1", required = true)
      @PathVariable Long id) {
    SubCategoryResponse subCategory = categoryMapper.toSubCategoryResponse(subCategoryService.getSubCategoryById(id));
    return ResponseWrapper.success(subCategory);
  }

  @GetMapping(value = "/api/v1/admin/subcategories/slug/{slug}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get subcategory by slug")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Success"),
      @ApiResponse(responseCode = "404", description = "Not found"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Forbidden")
  })
  public ResponseEntity<ResponseWrapper<SubCategoryResponse>> getSubCategoryBySlug(
      @Parameter(description = "SubCategory slug", example = "smartphones", required = true)
      @PathVariable String slug) {
    SubCategoryResponse subCategory = categoryMapper.toSubCategoryResponse(subCategoryService.getSubCategoryBySlug(slug));
    return ResponseWrapper.success(subCategory);
  }
}
