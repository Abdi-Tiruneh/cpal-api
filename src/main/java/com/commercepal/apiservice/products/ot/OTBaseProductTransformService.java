package com.commercepal.apiservice.products.ot;

import com.commercepal.apiservice.products.dto.MetaView;
import com.commercepal.apiservice.products.dto.PricingView;
import com.commercepal.apiservice.products.dto.PricingViewProvider;
import com.commercepal.apiservice.products.pricing.ProductPriceService;
import com.commercepal.apiservice.settings.foreign_exchange.ForeignExchangeService;
import com.commercepal.apiservice.shared.enums.SupportedCountry;
import com.commercepal.apiservice.shared.enums.SupportedCurrency;
import com.commercepal.apiservice.utils.CurrencyFormatUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Base transformation service providing common pricing and metadata extraction logic. Eliminates
 * code duplication between product list and detail transformations.
 */
@RequiredArgsConstructor
public abstract class OTBaseProductTransformService {

  protected final ProductPriceService priceService;
  protected final ForeignExchangeService foreignExchangeService;

  /**
   * Core pricing calculation logic (reusable for all product transformations). Handles USD
   * conversion, markup application, and discount calculations.
   *
   * @param regularPriceObj Regular price JSON object
   * @param promoPriceObj   Promotion price JSON object (nullable)
   * @param userCountry     User's country for currency and markup
   * @return Fully calculated pricing view with discounts
   */
  protected PricingView calculatePricing(
      JSONObject regularPriceObj,
      JSONObject promoPriceObj,
      SupportedCountry userCountry,
      SupportedCurrency targetCurrency) {

    BigDecimal rawOriginalUsd = regularPriceObj.optBigDecimal("OriginalPrice", BigDecimal.ZERO);
    String originalCurrencyCode = regularPriceObj.optString("OriginalCurrencyCode", "USD");
    BigDecimal rawPromoUsd = Optional.ofNullable(promoPriceObj)
        .map(obj -> obj.optBigDecimal("OriginalPrice", BigDecimal.ZERO))
        .orElse(null);

    // Use target currency
    BigDecimal exchangeRate = foreignExchangeService.getUsdToTargetRate(targetCurrency);

    // Convert and apply markup
    BigDecimal finalOriginalPrice = convertAndMarkup(rawOriginalUsd, exchangeRate, userCountry);

    BigDecimal effectiveUsdToConvert =
        (rawPromoUsd != null && rawPromoUsd.compareTo(rawOriginalUsd) < 0)
            ? rawPromoUsd
            : rawOriginalUsd;

    BigDecimal finalCurrentPrice = convertAndMarkup(effectiveUsdToConvert, exchangeRate,
        userCountry);

    // Discount calculation
    boolean hasDiscount = finalOriginalPrice.compareTo(finalCurrentPrice) > 0;

    BigDecimal discountAmount = BigDecimal.ZERO;
    int discountPercent = 0;
    String formattedDiscountAmount = null;
    String formattedDiscountPercentage = null;

    String currencyCode = targetCurrency.getCode();

    if (hasDiscount) {
      discountAmount = finalOriginalPrice.subtract(finalCurrentPrice);
      String formattedAmount = CurrencyFormatUtil.format(discountAmount, currencyCode);
      formattedDiscountAmount = "Save " + formattedAmount;

      discountPercent = finalOriginalPrice.subtract(finalCurrentPrice)
          .divide(finalOriginalPrice, 2, RoundingMode.HALF_UP)
          .multiply(BigDecimal.valueOf(100))
          .intValue();

      formattedDiscountPercentage = String.format("-%d%%", discountPercent);
    }

    // Provider Pricing View
    BigDecimal unitMarkup = priceService.calculateBaseMarkup(effectiveUsdToConvert, userCountry);

    PricingViewProvider pricingViewProvider = PricingViewProvider.builder()
        .providerCurrency(originalCurrencyCode)
        .providerUnitPrice(effectiveUsdToConvert)
        .unitMarkup(unitMarkup)
        .build();

    return PricingView.builder()
        .currency(targetCurrency)
        .currentPrice(finalCurrentPrice)
        .originalPrice(hasDiscount ? finalOriginalPrice : null)
        .discountAmount(discountAmount)
        .isOnDiscount(hasDiscount)
        .discountPercentage(discountPercent)
        .formattedDiscountPercentage(formattedDiscountPercentage)
        .formattedCurrentPrice(CurrencyFormatUtil.format(finalCurrentPrice, currencyCode))
        .formattedOriginalPrice(
            hasDiscount ? CurrencyFormatUtil.format(finalOriginalPrice, currencyCode) : null)
        .formattedDiscountAmount(formattedDiscountAmount)
        .pricingViewProvider(pricingViewProvider)
        .build();
  }

  /**
   * Convert USD amount to local currency and apply country-specific markup.
   *
   * @param usdAmount    Amount in USD
   * @param exchangeRate Exchange rate from USD to target currency
   * @param country      Target country for markup calculation
   * @return Final price with markup applied
   */
  protected BigDecimal convertAndMarkup(
      BigDecimal usdAmount,
      BigDecimal exchangeRate,
      SupportedCountry country) {

    // Convert USD to local currency
    BigDecimal convertedAmount = usdAmount.multiply(exchangeRate)
        .setScale(2, RoundingMode.HALF_UP);

    // Apply country-specific markup
    BigDecimal markup = priceService.calculateBaseMarkup(usdAmount, country);
    BigDecimal markupInLocalCurrency = markup.multiply(exchangeRate)
        .setScale(2, RoundingMode.HALF_UP);

    return convertedAmount.add(markupInLocalCurrency)
        .setScale(2, RoundingMode.HALF_UP);
  }

  /**
   * Extract metadata (ratings and review count) from featured values array.
   *
   * @param featuredValues JSON array containing featured values
   * @return MetaView with rating and review count
   */
  protected MetaView extractMeta(JSONArray featuredValues) {
    double rating = 0.0;
    int reviewCount = 0;

    if (featuredValues != null) {
      for (int i = 0; i < featuredValues.length(); i++) {
        JSONObject feature = featuredValues.optJSONObject(i);
        if (feature == null) {
          continue;
        }

        String name = feature.optString("Name", "");
        if ("rating".equals(name)) {
          rating = feature.optDouble("Value", 0.0);
        } else if ("reviews".equals(name)) {
          reviewCount = feature.optInt("Value", 0);
        }
      }
    }

    return MetaView.builder()
        .rating(rating)
        .reviewCount(reviewCount)
        .build();
  }
}
