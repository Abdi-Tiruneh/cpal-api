package com.commercepal.apiservice.orders.tracking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DeliveryAddressSummary
 * <p>
 * Summary of delivery address for order display. Shows customer-safe version with partial phone
 * masking.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAddressSummary {

  /**
   * Full name or address line
   */
  private String fullName;

  /**
   * Street address
   */
  private String streetAddress;

  /**
   * City
   */
  private String city;

  /**
   * Subcity/district
   */
  private String subcity;

  /**
   * Region/state
   */
  private String region;

  /**
   * Phone number (masked for security) Example: "+251 91*****901"
   */
  private String phoneNumber;

  /**
   * Postal code (if available)
   */
  private String postalCode;

  /**
   * Country
   */
  private String country;

  /**
   * Full formatted address as single string
   */
  private String formattedAddress;
}
