package com.commercepal.apiservice.categories.internal.controller;

import com.commercepal.apiservice.categories.api.CategoryService;
import com.commercepal.apiservice.categories.api.SubCategoryService;
import com.commercepal.apiservice.categories.internal.CategoryMapper;
import com.commercepal.apiservice.categories.internal.dto.SimpleCategoryResponse;
import com.commercepal.apiservice.categories.internal.dto.SimpleSubCategoryResponse;
import com.commercepal.apiservice.utils.response.ResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public REST controller for category and subcategory operations.
 * All endpoints are publicly accessible without authentication.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categories & SubCategories", description = "Public APIs for browsing categories and subcategories")
public class CategoryController {

  private final CategoryService categoryService;
  private final SubCategoryService subCategoryService;
  private final CategoryMapper categoryMapper;

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get all active categories")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Success")
  })
  public ResponseEntity<ResponseWrapper<List<SimpleCategoryResponse>>> getAllCategories() {
    log.info("[CATEGORY-API] GET /api/v1/categories - Request received");

    var categories = categoryService.getAllActiveCategories().stream()
        .map(category -> {
          var subCategories = subCategoryService.getActiveSubCategoriesByCategoryId(category.getId());
          return categoryMapper.toSimpleCategoryResponse(category, subCategories);
        })
        .toList();

    log.info("[CATEGORY-API] GET /api/v1/categories - Success: {} categories found", categories.size());

    return ResponseWrapper.success(categories);
  }

  @GetMapping(value = "/slug/{slug}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get category by slug")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Success"),
      @ApiResponse(responseCode = "404", description = "Category not found")
  })
  public ResponseEntity<ResponseWrapper<SimpleCategoryResponse>> getCategoryBySlug(
      @Parameter(description = "Category slug", example = "electronics", required = true)
      @PathVariable String slug) {
    log.info("[CATEGORY-API] GET /api/v1/categories/slug/{} - Request received", slug);

    var category = categoryService.getActiveCategoryBySlug(slug);
    var subCategories = subCategoryService.getActiveSubCategoriesByCategoryId(category.getId());
    SimpleCategoryResponse response = categoryMapper.toSimpleCategoryResponse(category, subCategories);

    log.info("[CATEGORY-API] GET /api/v1/categories/slug/{} - Success", slug);

    return ResponseWrapper.success(response);
  }

  @GetMapping(value = "/subcategories/category/{categoryId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get subcategories by category ID")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Success")
  })
  public ResponseEntity<ResponseWrapper<List<SimpleSubCategoryResponse>>> getSubCategoriesByCategoryId(
      @Parameter(description = "Category ID", example = "1", required = true)
      @PathVariable Long categoryId) {
    log.info("[SUBCATEGORY-API] GET /api/v1/categories/subcategories/category/{} - Request received", categoryId);

    var subCategories = subCategoryService.getActiveSubCategoriesByCategoryId(categoryId).stream()
        .map(categoryMapper::toSimpleSubCategoryResponse)
        .toList();

    log.info("[SUBCATEGORY-API] GET /api/v1/categories/subcategories/category/{} - Success: {} subcategories found",
        categoryId, subCategories.size());

    return ResponseWrapper.success(subCategories);
  }

  @GetMapping(value = "/subcategories/slug/{slug}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get subcategory by slug")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Success"),
      @ApiResponse(responseCode = "404", description = "Subcategory not found")
  })
  public ResponseEntity<ResponseWrapper<SimpleSubCategoryResponse>> getSubCategoryBySlug(
      @Parameter(description = "Subcategory slug", example = "smartphones", required = true)
      @PathVariable String slug) {
    log.info("[SUBCATEGORY-API] GET /api/v1/categories/subcategories/slug/{} - Request received", slug);

    var subCategory = subCategoryService.getActiveSubCategoryBySlug(slug);
    SimpleSubCategoryResponse response = categoryMapper.toSimpleSubCategoryResponse(subCategory);

    log.info("[SUBCATEGORY-API] GET /api/v1/categories/subcategories/slug/{} - Success", slug);

    return ResponseWrapper.success(response);
  }
}
