package com.commercepal.apiservice.shared.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SupportedCountry {
  ETHIOPIA("ET", "Ethiopia", SupportedCurrency.ETB, "+251", 9),
  INTERNATIONAL("INT", "International", SupportedCurrency.USD, "+1", 10),
  KENYA("KE", "Kenya", SupportedCurrency.KES, "+254", 9),
  SOMALIA("SO", "Somalia", SupportedCurrency.SOS, "+252", 8),
  UNITED_ARAB_EMIRATES("AE", "United Arab Emirates", SupportedCurrency.AED, "+971", 9);

  private final String code;
  private final String displayName;
  private final SupportedCurrency defaultCurrency;
  private final String phoneCode;
  private final Integer phoneNumberLength;

  @JsonCreator
  public static SupportedCountry fromCode(String code) {
    if (code == null) {
      return null;
    }
    for (SupportedCountry country : values()) {

      System.err.println(country.code);
      System.err.println(code);

      if (country.code.equalsIgnoreCase(code)) {
        return country;
      }
    }
    throw new IllegalArgumentException("Unsupported country code: " + code);
  }

  @JsonValue
  public String getCode() {
    return code;
  }
}
