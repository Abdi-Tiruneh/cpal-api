package com.commercepal.apiservice.shared.enums;

import lombok.Getter;

/**
 * Channel enum representing all client touchpoints and entry methods across the remittance platform
 * (agents, customers, transactions, etc.)
 * <p>
 * Used for: - User registration and onboarding tracking - Transaction origin and destination
 * tracking - Analytics and reporting - Channel-specific business rules - Marketing attribution -
 * Compliance and audit trails
 */
@Getter
public enum Channel {

  // WEB CHANNELS
  WEB("Web Browser", "Web application accessed via browser", false, true, "Web"),

  // MOBILE CHANNELS
  MOBILE_APP_ANDROID("Android App", "Android mobile application", true, true, "Mobile"),
  MOBILE_APP_IOS("iOS App", "iOS mobile application", true, true, "Mobile"),
  MOBILE_APP_WEBVIEW("Mobile Web", "Mobile browser web application", true, true, "Mobile"),

  // API CHANNELS
  API("REST API", "Representational State Transfer API integration", false, false, "API"),

  // ADMIN & INTERNAL CHANNELS
  ADMIN_PORTAL("Admin Portal", "Internal administrative portal", false, true, "Admin"),
  BACKEND_SYSTEM("Backend System", "Internal backend system process", false, false, "System");

  private final String displayName;
  private final String description;
  private final boolean requiresLocationTracking; // Whether this channel can track physical location
  private final boolean supportsRealTimeProcessing; // Whether this channel supports real-time transactions
  private final String category; // Channel category for grouping

  Channel(String displayName, String description, boolean requiresLocationTracking,
      boolean supportsRealTimeProcessing, String category) {
    this.displayName = displayName;
    this.description = description;
    this.requiresLocationTracking = requiresLocationTracking;
    this.supportsRealTimeProcessing = supportsRealTimeProcessing;
    this.category = category;
  }

  // BUSINESS LOGIC METHODS

  /**
   * Get all mobile app channels
   */
  public static Channel[] getMobileAppChannels() {
    return new Channel[]{
        MOBILE_APP_ANDROID,
        MOBILE_APP_IOS,
        MOBILE_APP_WEBVIEW
    };
  }

  /**
   * Get all web channels
   */
  public static Channel[] getWebChannels() {
    return new Channel[]{
        WEB,
        MOBILE_APP_WEBVIEW,
        ADMIN_PORTAL
    };
  }

  /**
   * Get all API channels
   */
  public static Channel[] getApiChannels() {
    return new Channel[]{
        API
    };
  }

  /**
   * Check if this channel is a mobile channel
   */
  public boolean isMobile() {
    return this == MOBILE_APP_ANDROID
        || this == MOBILE_APP_IOS
        || this == MOBILE_APP_WEBVIEW;
  }

  /**
   * Check if this channel is a web channel
   */
  public boolean isWeb() {
    return this == WEB
        || this == MOBILE_APP_WEBVIEW
        || this == ADMIN_PORTAL;
  }

  /**
   * Check if this channel is an API channel
   */
  public boolean isApi() {
    return this == API;
  }

  /**
   * Check if this channel is admin-only
   */
  public boolean isAdminOnly() {
    return this == ADMIN_PORTAL
        || this == BACKEND_SYSTEM;
  }

  /**
   * Get channel type description for reporting
   */
  public String getChannelTypeDescription() {
    if (isMobile()) {
      return "Mobile Application";
    }
    if (isWeb()) {
      return "Web Application";
    }
    if (isApi()) {
      return "API Integration";
    }
    return "Other Channel";
  }
}
