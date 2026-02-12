package com.commercepal.apiservice.shared.security;

final class SecurityConfigConstants {

  static final String[] PUBLIC_API_ENDPOINTS = {
      "/api/v1/auth/**",
      "/api/v1/auth/oauth2/**",
      "/api/v1/customers/register",
      "/api/v1/health/**",
      "/api/v1/public/**",
      "/api/docs/**",
      "/swagger-ui/**",
      "/v3/api-docs/**",
      "/actuator/health",
      "/actuator/info",
      "/api/v1/users/registration/**",
      "/api/v1/users/check-email/*",
      "/api/v1/users/check-phone/*",
      "/api/v1/credentials/password/forgot",
      "/api/v1/credentials/password/reset",
      "/api/v1/products",
      "/api/v1/products/{id}",
      "/api/v1/products/featured",
      "/api/v1/products/sku/**",
      "/api/v1/products/category/**",
      "/api/v1/categories/**", // Public category browsing endpoints
      "/api/v1/subcategories/**", // Public subcategory browsing endpoints
      "/api/v1/roles/active",
      "/api/enums/**",
      "/api/v1/reference-data/**",
      "/api/v1/admin/migrations/**"
  };
  static final String[] CUSTOMER_API_ENDPOINTS = {
      "/api/v1/orders/**",
      "/api/v1/cart/**",
      "/api/v1/customers/me/**",
      "/api/v1/customers/{id}/profile",
      "/api/v1/customers/{id}/addresses/**",
      "/api/v1/payments/**",
      "/api/v1/wishlist/**"
  };
  static final String[] MERCHANT_API_ENDPOINTS = {
      "/api/v1/merchant/products/**",
      "/api/v1/merchant/orders/**",
      "/api/v1/merchant/inventory/**",
      "/api/v1/merchant/analytics/**"
  };
  static final String[] ADMIN_API_ENDPOINTS = {
      "/api/v1/admin/**",
      "/api/v1/products/**",
      "/api/v1/orders/**",
      "/api/v1/customers/**",
      "/api/v1/roles/**",
      "/api/v1/categories/**",
      "/api/v1/inventory/**",
      "/api/v1/payments/**",
      "/api/v1/analytics/**",
      "/api/v1/reports/**"
  };
  static final String[] SYSTEM_API_ENDPOINTS = {
      "/api/v1/system/**"
  };
  static final String[] ACTUATOR_PUBLIC_ENDPOINTS = {
      "/actuator/health",
      "/actuator/info"
  };
  static final String[] ACTUATOR_SECURED_ENDPOINTS = {
      "/actuator/**"
  };
  static final String[] CORS_EXPOSED_HEADERS = {
      "Authorization",
      "X-Total-Count",
      "X-Page-Count",
      "X-Rate-Limit-Remaining",
      "X-Rate-Limit-Reset",
      "X-Request-ID",
      "Content-Range"
  };
  static final String CONTENT_SECURITY_POLICY = "default-src 'self'; "
      + "script-src 'self' 'unsafe-inline' 'unsafe-eval'; "
      + "style-src 'self' 'unsafe-inline'; "
      + "img-src 'self' data: https:; "
      + "font-src 'self' https:; "
      + "connect-src 'self' https:; "
      + "frame-ancestors 'none'; "
      + "base-uri 'self'; "
      + "form-action 'self'";
  static final String LOGOUT_SUCCESS_MESSAGE = "{\"message\":\"Logged out successfully\"}";
  static final String ACCESS_DENIED_RESPONSE_TEMPLATE = "{\"error\":\"Access Denied\",\"message\":\"Insufficient privileges\",\"timestamp\":\"%s\"}";
  static final String API_SECURITY_MATCHER = "/api/**";
  static final String ACTUATOR_SECURITY_MATCHER = "/actuator/**";
  static final String[] ADMIN_ROLES = {"ROLE_ADMIN", "ROLE_SUPER_ADMIN"};
  static final String[] MERCHANT_ROLES = {"ROLE_MERCHANT"};
  static final String[] CUSTOMER_ROLES = {"ROLE_CUSTOMER"};
  static final String[] SUPPORT_ROLES = {"ROLE_SUPPORT", "ROLE_MANAGER"};
  static final String ROLE_CUSTOMER = "ROLE_CUSTOMER";
  static final String ROLE_ADMIN = "ROLE_ADMIN";
  static final String ROLE_MERCHANT = "ROLE_MERCHANT";
  static final String ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN";
  static final String ROLE_SUPPORT = "ROLE_SUPPORT";
  static final String ROLE_MANAGER = "ROLE_MANAGER";
  static final String LOGOUT_URL = "/api/v1/auth/logout";
  static final String LOGOUT_AUDIT_REASON = "USER_LOGOUT";
  static final String LOGOUT_AUDIT_TOKEN_TYPE = "logout_token";
  static final String HEADER_AUTHORIZATION = "Authorization";
  static final String HEADER_CONTENT_TYPE_JSON = "application/json";
  static final String BEARER_PREFIX = "Bearer ";
  static final String ACTUATOR_REALM = "Actuator";
  static final String ACCESS_DENIED_EVENT = "ACCESS_DENIED";

  private SecurityConfigConstants() {
  }
}
