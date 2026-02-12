package com.commercepal.apiservice.products.ot;

import com.commercepal.apiservice.products.dto.CustomerReviewView;
import com.commercepal.apiservice.products.dto.ImageView;
import com.commercepal.apiservice.products.dto.MetaView;
import com.commercepal.apiservice.products.dto.PhysicalParametersView;
import com.commercepal.apiservice.products.dto.PricingView;
import com.commercepal.apiservice.products.dto.ProductAttributeView;
import com.commercepal.apiservice.products.dto.ProductCardResponse;
import com.commercepal.apiservice.products.dto.ProductDetailResponse;
import com.commercepal.apiservice.products.dto.ProductVariantView;
import com.commercepal.apiservice.products.dto.VariantConfiguratorView;
import com.commercepal.apiservice.products.dto.VideoView;
import com.commercepal.apiservice.products.pricing.ProductPriceService;
import com.commercepal.apiservice.products.service.ReviewTransformService;
import com.commercepal.apiservice.settings.foreign_exchange.ForeignExchangeService;
import com.commercepal.apiservice.shared.enums.SupportedCountry;
import com.commercepal.apiservice.shared.enums.SupportedCurrency;
import com.commercepal.apiservice.utils.CurrencyFormatUtil;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

/**
 * Professional transformation service for OT product detail responses. Handles complex variant
 * pricing, promotion mapping, and comprehensive data extraction. Extends base class for shared
 * pricing and metadata logic.
 */
@Slf4j
@Service
public class OTProductDetailTransformService extends OTBaseProductTransformService {

  private final ReviewTransformService reviewTransformService;
  private final OTProductTransformServiceOT otProductTransformService;

  public OTProductDetailTransformService(
      ProductPriceService priceService,
      ForeignExchangeService foreignExchangeService,
      ReviewTransformService reviewTransformService,
      OTProductTransformServiceOT otProductTransformService) {
    super(priceService, foreignExchangeService);
    this.reviewTransformService = reviewTransformService;
    this.otProductTransformService = otProductTransformService;
  }

  /**
   * Transform raw OT product detail JSON into structured ProductDetailResponse.
   *
   * @param item             Raw product JSON from OT API
   * @param providerReviews  Provider reviews JSON object containing review data
   * @param recommendedItems Recommended items JSON object containing product recommendations
   * @param userCountry      User's country for pricing calculation
   * @return Fully transformed product detail response
   */
  public ProductDetailResponse transform(JSONObject item, JSONObject providerReviews,
      JSONObject recommendedItems,
      SupportedCountry userCountry,
      SupportedCurrency targetCurrency) {
    try {
      // Extract basic information
      String id = item.optString("Id", "");
      String title = item.optString("OriginalTitle", item.optString("Title", "Untitled Product"));
      String provider = item.optString("ProviderType", "OT");

      // Extract vendor and brand info
      String brandName = item.optString("BrandName", "");
      String vendorName = item.optString("VendorName", "");

      // Extract category info
      String categoryId = item.optString("CategoryId", "");

      // Extract availability
      int masterQuantity = item.optInt("MasterQuantity", 0);
      boolean isSellAllowed = item.optBoolean("IsSellAllowed", true);
      String stuffStatus = item.optString("StuffStatus", "New");

      // Extract timestamps
      String createdTime = item.optString("CreatedTime", "");
      String updatedTime = item.optString("UpdatedTime", "");

      // Extract external URL
      String externalUrl = item.optString("ExternalItemUrl", "");

      // Extract ordering info
      int firstLotQuantity = item.optInt("FirstLotQuantity", 1);
      int nextLotQuantity = item.optInt("NextLotQuantity", 1);

      // Extract vendor score

      // Extract hierarchical configurators flag
      boolean hasHierarchicalConfigurators = item.optBoolean("HasHierarchicalConfigurators", false);

      // Build promotion map for variant pricing
      Map<String, JSONObject> promotionMap = buildPromotionMap(item.optJSONArray("Promotions"));

      // Extract complex components
      List<ImageView> images = extractAllImages(item.optJSONArray("Pictures"));
      ImageView mainImage = extractMainImage(item.optJSONArray("Pictures"));
      List<VideoView> videos = extractVideos(item.optJSONArray("Videos"));
      List<ProductVariantView> variants = extractVariants(
          item.optJSONArray("ConfiguredItems"),
          item.optJSONArray("Attributes"),
          promotionMap,
          userCountry,
          targetCurrency);

      PhysicalParametersView physicalParameters = extractPhysicalParameters(
          item.optJSONObject("PhysicalParameters"));
      MetaView meta = extractMeta(item.optJSONArray("FeaturedValues"));

      // Calculate base product pricing
      PricingView pricing = calculateBasePricing(item, userCountry, targetCurrency);

      // Transform customer reviews
      List<CustomerReviewView> customerReviews = reviewTransformService.transformProviderReviews(
          providerReviews);

      // Transform recommended products
      List<ProductCardResponse> recommendedProducts = extractRecommendedProducts(recommendedItems,
          userCountry, targetCurrency);

      // Parse description
      List<String> description = parseDescription(item);

      // Determine status
      String status = (masterQuantity > 0 && isSellAllowed) ? "AVAILABLE" : "OUT_OF_STOCK";

      // Build and return response
      return ProductDetailResponse.builder()
          .id(id)
          .title(title)
          .provider(provider)
          .brandName(brandName)
          .vendorName(vendorName)
          .categoryId(categoryId)
          .status(status)
          .stockLevel(masterQuantity)
          .isSellAllowed(isSellAllowed)
          .stuffStatus(stuffStatus)
          .pricing(pricing)
          .images(images)
          .mainImage(mainImage)
          .videos(videos)
          .variants(variants)
          .hasHierarchicalConfigurators(hasHierarchicalConfigurators)
          .description(description)
          .physicalParameters(physicalParameters)
          .meta(meta)
          .customerReviews(customerReviews)
          .recommendedProducts(recommendedProducts)
          .externalUrl(externalUrl)
          .minOrderQuantity(firstLotQuantity)
          .quantityStep(nextLotQuantity)
          .createdTime(createdTime)
          .updatedTime(updatedTime)
          .build();

    } catch (Exception e) {
      log.error("Failed to transform product detail for item: {}", item.optString("Id"), e);
      throw new RuntimeException("Product detail transformation failed", e);
    }
  }

  /**
   * Build a map of SKU ID -> Promotion data for quick lookup.
   */
  private Map<String, JSONObject> buildPromotionMap(JSONArray promotions) {
    Map<String, JSONObject> promoMap = new HashMap<>();

    if (promotions == null || promotions.isEmpty()) {
      return promoMap;
    }

    for (int i = 0; i < promotions.length(); i++) {
      JSONObject promotion = promotions.optJSONObject(i);
      if (promotion == null) {
        continue;
      }

      JSONArray configuredItems = promotion.optJSONArray("ConfiguredItems");
      if (configuredItems == null) {
        continue;
      }

      for (int j = 0; j < configuredItems.length(); j++) {
        JSONObject configItem = configuredItems.optJSONObject(j);
        if (configItem != null) {
          String skuId = configItem.optString("Id");
          if (!skuId.isEmpty()) {
            promoMap.put(skuId, configItem);
          }
        }
      }
    }

    return promoMap;
  }

  /**
   * Extract all product images.
   */
  private List<ImageView> extractAllImages(JSONArray pictures) {
    List<ImageView> imageList = new ArrayList<>();

    if (pictures == null) {
      return imageList;
    }

    for (int i = 0; i < pictures.length(); i++) {
      JSONObject pic = pictures.optJSONObject(i);
      if (pic == null) {
        continue;
      }

      String thumbnail = Optional.ofNullable(pic.optJSONObject("Small"))
          .map(obj -> obj.optString("Url"))
          .orElse(null);

      String main = Optional.ofNullable(pic.optJSONObject("Medium"))
          .map(obj -> obj.optString("Url"))
          .orElse(null);

      if (thumbnail != null || main != null) {
        imageList.add(ImageView.builder()
            .thumbnail(thumbnail)
            .main(main)
            .build());
      }
    }

    return imageList;
  }

  /**
   * Extract the main/primary product image.
   */
  private ImageView extractMainImage(JSONArray pictures) {
    if (pictures == null) {
      return null;
    }

    // First, try to find the image marked as "IsMain"
    for (int i = 0; i < pictures.length(); i++) {
      JSONObject pic = pictures.optJSONObject(i);
      if (pic != null && pic.optBoolean("IsMain", false)) {
        String thumbnail = Optional.ofNullable(pic.optJSONObject("Small"))
            .map(obj -> obj.optString("Url"))
            .orElse(null);

        String main = Optional.ofNullable(pic.optJSONObject("Medium"))
            .map(obj -> obj.optString("Url"))
            .orElse(null);

        return ImageView.builder()
            .thumbnail(thumbnail)
            .main(main)
            .build();
      }
    }

    // Fallback: Use the first image
    if (!pictures.isEmpty()) {
      JSONObject pic = pictures.optJSONObject(0);
      if (pic != null) {
        String thumbnail = Optional.ofNullable(pic.optJSONObject("Small"))
            .map(obj -> obj.optString("Url"))
            .orElse(null);

        String main = Optional.ofNullable(pic.optJSONObject("Medium"))
            .map(obj -> obj.optString("Url"))
            .orElse(null);

        return ImageView.builder()
            .thumbnail(thumbnail)
            .main(main)
            .build();
      }
    }

    return null;
  }

  /**
   * Extract product videos.
   */
  private List<VideoView> extractVideos(JSONArray videos) {
    List<VideoView> videoList = new ArrayList<>();

    if (videos == null) {
      return videoList;
    }

    for (int i = 0; i < videos.length(); i++) {
      JSONObject video = videos.optJSONObject(i);
      if (video == null) {
        continue;
      }

      String url = video.optString("Url", null);
      String previewUrl = video.optString("PreviewUrl", null);

      if (url != null) {
        videoList.add(VideoView.builder()
            .url(url)
            .previewUrl(previewUrl)
            .build());
      }
    }

    return videoList;
  }

  /**
   * Extract product variants with pricing from promotions.
   */
  private List<ProductVariantView> extractVariants(
      JSONArray configuredItems,
      JSONArray attributes,
      Map<String, JSONObject> promotionMap,
      SupportedCountry userCountry,
      SupportedCurrency targetCurrency) {
    List<ProductVariantView> variantList = new ArrayList<>();

    if (configuredItems == null) {
      return variantList;
    }

    // Build attribute lookup map for configurator details
    Map<String, ProductAttributeView> attributeMap = buildAttributeMap(attributes);

    for (int i = 0; i < configuredItems.length(); i++) {
      JSONObject configItem = configuredItems.optJSONObject(i);
      if (configItem == null) {
        continue;
      }

      String skuId = configItem.optString("Id", "");
      int quantity = configItem.optInt("Quantity", 0);
      int salesCount = configItem.optInt("SalesCount", 0);

      // Extract configurators
      List<VariantConfiguratorView> configurators = extractConfigurators(
          configItem.optJSONArray("Configurators"),
          attributeMap);

      // Check if this variant has promotion pricing
      JSONObject promoData = promotionMap.get(skuId);
      PricingView variantPricing = null;

      if (promoData != null) {
        // Calculate pricing with promotion
        variantPricing = calculateVariantPricing(
            configItem.optJSONObject("Price"),
            promoData.optJSONObject("Price"),
            userCountry,
            targetCurrency);
      }

      variantList.add(ProductVariantView.builder()
          .configId(skuId)
          .quantity(quantity)
          .salesCount(salesCount)
          .pricing(variantPricing)
          .configurators(configurators)
          .build());
    }

    return variantList;
  }

  /**
   * Build a map of attribute value ID -> ProductAttributeView for quick lookup.
   */
  private Map<String, ProductAttributeView> buildAttributeMap(JSONArray attributes) {
    Map<String, ProductAttributeView> attrMap = new HashMap<>();

    if (attributes == null) {
      return attrMap;
    }

    for (int i = 0; i < attributes.length(); i++) {
      JSONObject attr = attributes.optJSONObject(i);
      if (attr == null) {
        continue;
      }

      String vid = attr.optString("Vid", "");
      if (!vid.isEmpty()) {
        ProductAttributeView attrView = ProductAttributeView.builder()
            .propertyName(attr.optString("PropertyName", ""))
            .value(attr.optString("Value", ""))
            .isConfigurator(attr.optBoolean("IsConfigurator", false))
            .imageUrl(attr.optString("ImageUrl", null))
            .miniImageUrl(attr.optString("MiniImageUrl", null))
            .build();

        attrMap.put(vid, attrView);
      }
    }

    return attrMap;
  }

  /**
   * Extract configurators for a variant with complete attribute details enriched.
   */
  private List<VariantConfiguratorView> extractConfigurators(
      JSONArray configurators,
      Map<String, ProductAttributeView> attributeMap) {
    List<VariantConfiguratorView> configList = new ArrayList<>();

    if (configurators == null) {
      return configList;
    }

    for (int i = 0; i < configurators.length(); i++) {
      JSONObject config = configurators.optJSONObject(i);
      if (config == null) {
        continue;
      }

      String vid = config.optString("Vid", "");
      String pid = config.optString("Pid", "");

      // Look up complete attribute details from the attributes array
      ProductAttributeView attr = attributeMap.get(vid);

      if (attr != null) {
        // Build enriched configurator with all attribute details
        configList.add(VariantConfiguratorView.builder()
            .propertyId(pid)
            .valueId(vid)
            .propertyName(attr.propertyName())
            .value(attr.value())
            .imageUrl(attr.imageUrl())
            .miniImageUrl(attr.miniImageUrl())
            .isConfigurator(attr.isConfigurator())
            .build());
      } else {
        // Fallback: Create configurator with just IDs if attribute not found
        log.warn("Attribute not found for Vid: {} in product configurators", vid);
        configList.add(VariantConfiguratorView.builder()
            .propertyId(pid)
            .valueId(vid)
            .propertyName("")
            .value("")
            .imageUrl(null)
            .miniImageUrl(null)
            .isConfigurator(true)
            .build());
      }
    }

    return configList;
  }

  /**
   * Extract physical parameters.
   */
  private PhysicalParametersView extractPhysicalParameters(JSONObject physicalParams) {
    if (physicalParams == null) {
      return null;
    }

    return PhysicalParametersView.builder()
        .length(physicalParams.optDouble("Length", 0.0))
        .width(physicalParams.optDouble("Width", 0.0))
        .height(physicalParams.optDouble("Height", 0.0))
        .weight(physicalParams.optDouble("Weight", 0.0))
        .build();
  }

  /**
   * Calculate base product pricing (without variant-specific pricing). Extracts promotion price
   * from Promotions[0].Price if available.
   */
  private PricingView calculateBasePricing(JSONObject item, SupportedCountry userCountry,
      SupportedCurrency targetCurrency) {
    JSONObject regularPriceObj = item.optJSONObject("Price");

    // Extract base promotion price from Promotions array
    JSONObject promoPriceObj = extractBasePromotionPrice(item.optJSONArray("Promotions"));

    if (regularPriceObj == null || !regularPriceObj.has("OriginalPrice")) {
      // No base pricing available, return empty pricing
      return PricingView.builder()
          .currency(targetCurrency)
          .currentPrice(BigDecimal.ZERO)
          .originalPrice(null)
          .discountAmount(BigDecimal.ZERO)
          .isOnDiscount(false)
          .discountPercentage(0)
          .formattedDiscountPercentage(null)
          .formattedCurrentPrice(
              CurrencyFormatUtil.format(BigDecimal.ZERO, targetCurrency.getCode()))
          .formattedOriginalPrice(null)
          .formattedDiscountAmount(null)
          .build();
    }

    return calculatePricing(regularPriceObj, promoPriceObj, userCountry, targetCurrency);
  }

  /**
   * Extract base-level promotion price from Promotions array. Returns Promotions[0].Price if
   * available, null otherwise.
   */
  private JSONObject extractBasePromotionPrice(JSONArray promotions) {
    if (promotions == null || promotions.isEmpty()) {
      return null;
    }

    // Get first promotion
    JSONObject firstPromotion = promotions.optJSONObject(0);
    if (firstPromotion == null) {
      return null;
    }

    // Extract base-level Price object
    JSONObject promoPriceObj = firstPromotion.optJSONObject("Price");

    // Check if Price object has actual data
    if (promoPriceObj != null && promoPriceObj.has("OriginalPrice")) {
      return promoPriceObj;
    }

    return null;
  }

  /**
   * Calculate variant-specific pricing.
   */
  private PricingView calculateVariantPricing(
      JSONObject regularPriceObj,
      JSONObject promoPriceObj,
      SupportedCountry userCountry,
      SupportedCurrency targetCurrency) {
    if (regularPriceObj == null || !regularPriceObj.has("OriginalPrice")) {
      return null; // No pricing for this variant
    }

    return calculatePricing(regularPriceObj, promoPriceObj, userCountry, targetCurrency);
  }

  /**
   * Parse product description using OTProductDescriptionParser.
   */
  /**
   * Parse product description using OTProductDescriptionParser.
   */
  private List<String> parseDescription(JSONObject item) {
    try {
      // Try to get HTML description from common fields
      String htmlDescription = item.optString("Description", "");

      if (htmlDescription.isEmpty()) {
        htmlDescription = item.optString("HtmlDescription", "");
      }

      if (htmlDescription.isEmpty()) {
        return new ArrayList<>();
      }

      return OTProductDescriptionParser.parseProductDescription(htmlDescription);
    } catch (Exception e) {
      log.warn("Failed to parse product description for item: {}", item.optString("Id"), e);
      return new ArrayList<>();
    }
  }

  /**
   * Extract and transform recommended products from RecommendedItems JSON.
   *
   * @param recommendedItems Recommended items JSON object
   * @param userCountry      User's country for pricing calculation
   * @return List of ProductCardResponse objects
   */
  private List<ProductCardResponse> extractRecommendedProducts(JSONObject recommendedItems,
      SupportedCountry userCountry, SupportedCurrency targetCurrency) {
    List<ProductCardResponse> products = new ArrayList<>();

    if (recommendedItems == null || recommendedItems.isEmpty()) {
      log.debug("Recommended items object is null or empty");
      return products;
    }

    JSONArray itemsArray = recommendedItems.optJSONArray("Content");
    if (itemsArray == null || itemsArray.isEmpty()) {
      log.debug("Recommended items array is null or empty");
      return products;
    }

    for (int i = 0; i < itemsArray.length(); i++) {
      JSONObject itemJson = itemsArray.optJSONObject(i);
      if (itemJson == null) {
        log.warn("Skipping null recommended item at index {}", i);
        continue;
      }

      try {
        ProductCardResponse productCard = otProductTransformService.transform(itemJson,
            userCountry, targetCurrency);
        products.add(productCard);
      } catch (Exception e) {
        log.error("Failed to transform recommended item at index {}: {}", i, e.getMessage(), e);
        // Continue processing other items instead of failing completely
      }
    }

    log.info("Successfully transformed {} out of {} recommended items", products.size(),
        itemsArray.length());
    return products;
  }
}
