package com.commercepal.apiservice.products.pricing;

import com.commercepal.apiservice.shared.enums.SupportedCountry;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public final class ProductPriceService {

  private static final int SCALE = 2;
  private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
  private static final BigDecimal DEFAULT_MARKUP_RATE = BigDecimal.valueOf(0.3);

  private static final Map<SupportedCountry, BigDecimal> COUNTRY_MARKUPS;

  static {
    COUNTRY_MARKUPS = new EnumMap<>(SupportedCountry.class);
    COUNTRY_MARKUPS.put(SupportedCountry.ETHIOPIA, BigDecimal.valueOf(0.30)); // 30%
    COUNTRY_MARKUPS.put(SupportedCountry.INTERNATIONAL, BigDecimal.valueOf(0.30)); // 30%
    COUNTRY_MARKUPS.put(SupportedCountry.KENYA, BigDecimal.valueOf(0.05)); // 5%
    COUNTRY_MARKUPS.put(SupportedCountry.SOMALIA, BigDecimal.valueOf(0.05)); // 5%
    COUNTRY_MARKUPS.put(SupportedCountry.UNITED_ARAB_EMIRATES, BigDecimal.valueOf(0.01)); // 1%
  }

  private static BigDecimal getMarkupRate(SupportedCountry country) {
    return COUNTRY_MARKUPS.getOrDefault(country, DEFAULT_MARKUP_RATE);
  }

  /**
   * Calculates markup amount for a single unit price based on delivery country.
   */
  public BigDecimal calculateBaseMarkup(BigDecimal unitPrice, SupportedCountry country) {
    SupportedCountry targetCountry = Objects.requireNonNullElse(country,
        SupportedCountry.INTERNATIONAL);
    return unitPrice.multiply(getMarkupRate(targetCountry)).setScale(SCALE, ROUNDING_MODE);
  }

  /**
   * Calculates total markup for a given quantity.
   */
  public BigDecimal calculateTotalMarkup(BigDecimal unitPrice, int quantity,
      SupportedCountry country) {
    BigDecimal unitMarkup = calculateBaseMarkup(unitPrice, country);
    return unitMarkup.multiply(BigDecimal.valueOf(quantity)).setScale(SCALE, ROUNDING_MODE);
  }

  /**
   * Calculates unit price after markup.
   */
  public BigDecimal calculateUnitPriceWithMarkup(BigDecimal unitPrice, SupportedCountry country) {
    return unitPrice.add(calculateBaseMarkup(unitPrice, country))
        .setScale(SCALE, ROUNDING_MODE);
  }

  /**
   * Calculates total price (unit price + markup) for given quantity.
   */
  public BigDecimal calculateTotalPriceWithMarkup(BigDecimal unitPrice, int quantity,
      SupportedCountry country) {
    BigDecimal totalBase = unitPrice.multiply(BigDecimal.valueOf(quantity));
    BigDecimal totalMarkup = calculateTotalMarkup(unitPrice, quantity, country);
    return totalBase.add(totalMarkup).setScale(SCALE, ROUNDING_MODE);
  }
}