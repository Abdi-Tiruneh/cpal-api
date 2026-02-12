package com.commercepal.apiservice.users.customer.address;

public enum AddressSourceType {
  /**
   * User added the address manually from the app/web.
   */
  MANUAL,

  /**
   * Address was selected using search results from Google Maps API.
   */
  GOOGLE_MAPS,

  /**
   * User dropped a pin on the map; system reverse-geocoded it.
   */
  MAP_PIN,

  /**
   * Address collected during on-site delivery by couriers or field agents.
   */
  DELIVERY_AGENT,

  /**
   * Auto-filled using previously used or recommended addresses.
   */
  PREVIOUS_ADDRESS,

  /**
   * Address obtained from KYC / national ID sources.
   */
  VERIFIED_ID,

  /**
   * Auto-filled by system based on GPS location (mobile app).
   */
  GPS_LOCATION,

  /**
   * Address fetched from a partner system (e.g., logistics provider).
   */
  EXTERNAL_PROVIDER
}
