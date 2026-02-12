package com.commercepal.apiservice.products.ot;

import com.commercepal.apiservice.products.dto.ImageView;
import com.commercepal.apiservice.products.dto.MetaView;
import com.commercepal.apiservice.products.dto.PricingView;
import com.commercepal.apiservice.products.dto.ProductCardResponse;
import com.commercepal.apiservice.products.pricing.ProductPriceService;
import com.commercepal.apiservice.settings.foreign_exchange.ForeignExchangeService;
import com.commercepal.apiservice.shared.enums.SupportedCountry;
import com.commercepal.apiservice.shared.enums.SupportedCurrency;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

/**
 * Transformation service for product list/card responses. Extends base class for shared pricing and
 * metadata logic.
 */
@Service
public class OTProductTransformServiceOT extends OTBaseProductTransformService {

  public OTProductTransformServiceOT(
      ProductPriceService priceService,
      ForeignExchangeService foreignExchangeService) {
    super(priceService, foreignExchangeService);
  }

  /**
   * Transform raw OT product JSON into ProductCardResponse.
   *
   * @param item        Raw product JSON from OT API
   * @param userCountry User's country for pricing calculation
   * @return Transformed product card response
   */
  public ProductCardResponse transform(JSONObject item, SupportedCountry userCountry,
      SupportedCurrency targetCurrency) {
    // Extract base data
    String id = item.getString("Id");
    String title = item.optString("OriginalTitle", "Untitled Product");
    String provider = item.optString("ProviderType", "OT");
    int quantity = item.optInt("MasterQuantity", 0);

    // Build components using base class methods
    ImageView images = extractImages(item.optJSONArray("Pictures"));
    JSONObject promoPriceObj = item.optJSONObject("PromotionPrice");
    JSONObject regularPriceObj = item.optJSONObject("Price");

    PricingView pricing = calculatePricing(regularPriceObj, promoPriceObj, userCountry,
        targetCurrency);
    MetaView meta = extractMeta(item.optJSONArray("FeaturedValues"));

    // Build final response
    return ProductCardResponse.builder()
        .id(id)
        .title(title)
        .provider(provider)
        .status(quantity > 0 ? "AVAILABLE" : "OUT_OF_STOCK")
        .stockLevel(quantity)
        .pricing(pricing)
        .images(images)
        .meta(meta)
        .build();
  }

  /**
   * Extract primary product image from pictures array.
   *
   * @param pictures JSON array of product pictures
   * @return ImageView with thumbnail and main URLs
   */
  private ImageView extractImages(JSONArray pictures) {
    String thumb = null;
    String main = null;

    if (pictures != null) {
      for (int i = 0; i < pictures.length(); i++) {
        JSONObject pic = pictures.optJSONObject(i);
        if (pic == null) {
          continue;
        }

        // Prioritize "IsMain" picture
        if (pic.optBoolean("IsMain")) {
          thumb = pic.optJSONObject("Small").optString("Url");
          main = pic.optJSONObject("Medium").optString("Url");
          break;
        }
        // Fallback: Use the first picture as default
        if (thumb == null) {
          thumb = pic.optJSONObject("Small").optString("Url");
          main = pic.optJSONObject("Medium").optString("Url");
        }
      }
    }
    return ImageView.builder().thumbnail(thumb).main(main).build();
  }
}