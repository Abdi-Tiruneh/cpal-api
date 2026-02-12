package com.commercepal.apiservice.categories.internal;

import com.commercepal.apiservice.categories.api.Category;
import com.commercepal.apiservice.categories.api.CategoryRepository;
import com.commercepal.apiservice.categories.api.SubCategory;
import com.commercepal.apiservice.categories.api.SubCategoryRepository;
import com.commercepal.apiservice.categories.enums.CategoryStatus;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Data initialization service for categories and subcategories.
 * Seeds default categories and subcategories on application startup if tables are empty.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(4)
public class CategoryDataInitializer implements CommandLineRunner {

  private final CategoryRepository categoryRepository;
  private final SubCategoryRepository subCategoryRepository;

  private static final List<String> PROVIDER_IDS = Arrays.asList(
      "sh-1925", "sh-12476", "sh-1738", "sh-2137", "sh-1764", "sh-1770"
  );

  private final Random random = new Random();

  @Override
  @Transactional
  public void run(String... args) {
    log.info("==========================================================");
    log.info("Starting Category and SubCategory Data Initialization...");
    log.info("==========================================================");

    try {
      seedCategoriesAndSubCategories();
      log.info("==========================================================");
      log.info("Category and SubCategory Data Initialization Completed Successfully!");
      log.info("==========================================================");
    } catch (Exception e) {
      log.error("[CATEGORY_SEEDER] Error during category data initialization", e);
      throw new RuntimeException("Failed to initialize category data", e);
    }
  }

  private void seedCategoriesAndSubCategories() {
    // Check if categories table is empty
    long categoryCount = categoryRepository.count();
    if (categoryCount > 0) {
      log.info("[CATEGORY_SEEDER] Categories table is not empty ({} categories found), skipping seeding", categoryCount);
      return;
    }

    // Check if subcategories table is empty
    long subCategoryCount = subCategoryRepository.count();
    if (subCategoryCount > 0) {
      log.info("[CATEGORY_SEEDER] Subcategories table is not empty ({} subcategories found), skipping seeding", subCategoryCount);
      return;
    }

    log.info("[CATEGORY_SEEDER] Both categories and subcategories tables are empty, proceeding with seeding");

    // Define categories with their descriptions and subcategories
    Map<String, CategoryInfo> categoryData = new LinkedHashMap<>();
    
    categoryData.put("Technology", new CategoryInfo(
        "Discover cutting-edge technology products including computers, smart devices, and innovative gadgets designed to enhance your digital lifestyle and productivity.",
        List.of(
            new SubCategoryInfo("Laptop", "High-performance laptops and notebooks for work, gaming, and creative projects. Featuring the latest processors, graphics cards, and premium displays."),
            new SubCategoryInfo("Smart Watch", "Advanced wearable technology that tracks your fitness, manages notifications, and keeps you connected on the go."),
            new SubCategoryInfo("Tablet", "Versatile tablets perfect for entertainment, productivity, and creativity. Ideal for reading, streaming, and mobile computing."),
            new SubCategoryInfo("Desktop", "Powerful desktop computers and workstations built for demanding tasks, gaming, and professional applications."),
            new SubCategoryInfo("Accessories", "Essential tech accessories including cables, chargers, cases, stands, and peripherals to complement your devices.")
        )
    ));
    
    categoryData.put("Fashion", new CategoryInfo(
        "Explore the latest fashion trends and styles. From casual wear to formal attire, discover clothing and accessories that express your unique personality.",
        List.of()
    ));
    
    categoryData.put("Watch", new CategoryInfo(
        "Elegant timepieces that combine precision craftsmanship with timeless design. From classic analog watches to modern smartwatches, find the perfect watch for every occasion.",
        List.of(
            new SubCategoryInfo("Men's Watch", "Sophisticated men's watches featuring premium materials, precision movements, and classic designs suitable for business and casual wear."),
            new SubCategoryInfo("Women's Watches", "Elegant women's watches with refined aesthetics, featuring delicate designs, premium finishes, and versatile styles for any occasion."),
            new SubCategoryInfo("Smart watch", "Intelligent smartwatches that seamlessly integrate with your digital life, offering fitness tracking, notifications, and advanced connectivity features.")
        )
    ));
    
    categoryData.put("Electronics", new CategoryInfo(
        "Comprehensive range of electronic devices and components. From audio equipment to home entertainment systems, find quality electronics for every need.",
        List.of()
    ));
    
    categoryData.put("Home Appliances", new CategoryInfo(
        "Essential home appliances designed to simplify your daily life. Discover kitchen appliances, cleaning equipment, and smart home solutions that make household tasks effortless.",
        List.of()
    ));
    
    categoryData.put("Automotive", new CategoryInfo(
        "Premium automotive products and accessories for car enthusiasts. From maintenance supplies to performance upgrades, find everything you need for your vehicle.",
        List.of()
    ));
    
    categoryData.put("Sport and Outdoor", new CategoryInfo(
        "Gear and equipment for active lifestyles and outdoor adventures. Discover sports equipment, camping gear, and fitness accessories for all your athletic pursuits.",
        List.of()
    ));
    
    categoryData.put("Cosmetics", new CategoryInfo(
        "Premium beauty and personal care products. Discover skincare, makeup, and fragrance collections from trusted brands to enhance your natural beauty.",
        List.of(
            new SubCategoryInfo("Lotion", "Nourishing body lotions and moisturizers formulated with premium ingredients to keep your skin soft, hydrated, and healthy."),
            new SubCategoryInfo("Makeup", "Professional-quality makeup products including foundations, lipsticks, eyeshadows, and beauty tools to create stunning looks."),
            new SubCategoryInfo("Perfume", "Luxurious fragrances and colognes from renowned brands. Discover signature scents for men and women that leave a lasting impression.")
        )
    ));

    int categoriesCreated = 0;
    int subCategoriesCreated = 0;

    int displayOrder = 1;

    for (Map.Entry<String, CategoryInfo> entry : categoryData.entrySet()) {
      String categoryName = entry.getKey();
      CategoryInfo categoryInfo = entry.getValue();
      String categorySlug = generateSlug(categoryName);

      String categoryCode = generateCode(categoryName);
      String providerId = getRandomProviderId();
      log.info("[CATEGORY_SEEDER] Creating category: {} (slug: {}, code: {}, provider: {})", 
          categoryName, categorySlug, categoryCode, providerId);

      // Check for existing category with same slug or code before creating
      if (categoryRepository.existsBySlug(categorySlug)) {
        log.warn("[CATEGORY_SEEDER] Category with slug '{}' already exists, skipping", categorySlug);
        continue;
      }
      if (categoryRepository.existsByCode(categoryCode)) {
        log.warn("[CATEGORY_SEEDER] Category with code '{}' already exists, skipping", categoryCode);
        continue;
      }

      Category category = Category.builder()
          .name(categoryName)
          .slug(categorySlug)
          .code(categoryCode)
          .description(categoryInfo.description)
          .status(CategoryStatus.ACTIVE)
          .displayOrder(displayOrder++)
          .providerId(providerId)
          .build();

      try {
        category = categoryRepository.save(category);
        log.info("[CATEGORY_SEEDER] Created category: ID={}, name={}, slug={}",
            category.getId(), category.getName(), category.getSlug());
        categoriesCreated++;
      } catch (org.springframework.dao.DataIntegrityViolationException e) {
        log.error("[CATEGORY_SEEDER] Failed to create category '{}' due to constraint violation: {}", 
            categoryName, e.getMessage());
        // Continue with next category instead of failing entire seeding
        continue;
      }

      // Create subcategories for this category
      int subDisplayOrder = 1;
      for (SubCategoryInfo subCategoryInfo : categoryInfo.subCategories) {
        String subCategorySlug = generateSlug(subCategoryInfo.name);

        log.info("[CATEGORY_SEEDER] Creating subcategory: {} (slug: {}, provider: {}) under category: {}",
            subCategoryInfo.name, subCategorySlug, providerId, categoryName);

        // Check for existing subcategory with same slug before creating
        if (subCategoryRepository.findBySlug(subCategorySlug).isPresent()) {
          log.warn("[CATEGORY_SEEDER] Subcategory with slug '{}' already exists, skipping", subCategorySlug);
          continue;
        }

        SubCategory subCategory = SubCategory.builder()
            .name(subCategoryInfo.name)
            .slug(subCategorySlug)
            .description(subCategoryInfo.description)
            .status(CategoryStatus.ACTIVE)
            .displayOrder(subDisplayOrder++)
            .providerId(providerId)
            .category(category)
            .build();

        try {
          subCategory = subCategoryRepository.save(subCategory);
          log.info("[CATEGORY_SEEDER] Created subcategory: ID={}, name={}, slug={}, category={}",
              subCategory.getId(), subCategory.getName(), subCategory.getSlug(), categoryName);
          subCategoriesCreated++;
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
          log.error("[CATEGORY_SEEDER] Failed to create subcategory '{}' due to constraint violation: {}", 
              subCategoryInfo.name, e.getMessage());
          // Continue with next subcategory instead of failing entire seeding
          continue;
        }
      }
    }

    log.info("[CATEGORY_SEEDER] Seeding complete: {} categories created, {} subcategories created",
        categoriesCreated, subCategoriesCreated);
  }

  /**
   * Randomly select a provider ID from the available list.
   */
  private String getRandomProviderId() {
    return PROVIDER_IDS.get(random.nextInt(PROVIDER_IDS.size()));
  }

  /**
   * Generate a URL-friendly slug from a name.
   * Converts to lowercase, replaces spaces with hyphens, and removes special characters.
   * Returns "default" if the result would be empty.
   */
  private String generateSlug(String name) {
    if (name == null || name.trim().isEmpty()) {
      return "default";
    }
    String slug = name.trim()
        .toLowerCase()
        .replaceAll("[^a-z0-9\\s-]", "") // Remove special characters except spaces and hyphens
        .replaceAll("\\s+", "-") // Replace spaces with hyphens
        .replaceAll("-+", "-") // Replace multiple hyphens with single hyphen
        .replaceAll("^-|-$", ""); // Remove leading/trailing hyphens
    
    // If slug is empty after processing (e.g., all special characters), return a default
    if (slug.isEmpty()) {
      return "default-" + System.currentTimeMillis();
    }
    return slug;
  }

  /**
   * Generate a unique category code from a name.
   * Takes first letters of each word and converts to uppercase.
   * For single words, takes first 4 characters.
   * Example: "Home Appliances" -> "HA", "Electronics" -> "ELEC", "Technology" -> "TECH"
   * Returns "DEF" if the result would be empty.
   */
  private String generateCode(String name) {
    if (name == null || name.trim().isEmpty()) {
      return "DEF";
    }
    String[] words = name.trim().split("\\s+");
    if (words.length == 1) {
      // Single word: take first 4 characters
      String word = words[0].toUpperCase().replaceAll("[^A-Z0-9]", "");
      if (word.isEmpty()) {
        return "DEF";
      }
      return word.length() > 4 ? word.substring(0, 4) : word;
    } else {
      // Multiple words: take first letter of each word
      StringBuilder code = new StringBuilder();
      for (String word : words) {
        if (!word.isEmpty()) {
          String firstChar = word.replaceAll("[^A-Za-z0-9]", "").substring(0, Math.min(1, word.length()));
          if (!firstChar.isEmpty()) {
            code.append(firstChar.toUpperCase().charAt(0));
          }
        }
      }
      String result = code.toString().toUpperCase();
      return result.isEmpty() ? "DEF" : result;
    }
  }

  /**
   * Internal class to hold category information including description and subcategories.
   */
  private static class CategoryInfo {
    final String description;
    final List<SubCategoryInfo> subCategories;

    CategoryInfo(String description, List<SubCategoryInfo> subCategories) {
      this.description = description;
      this.subCategories = subCategories;
    }
  }

  /**
   * Internal class to hold subcategory information including description.
   */
  private static class SubCategoryInfo {
    final String name;
    final String description;

    SubCategoryInfo(String name, String description) {
      this.name = name;
      this.description = description;
    }
  }
}
