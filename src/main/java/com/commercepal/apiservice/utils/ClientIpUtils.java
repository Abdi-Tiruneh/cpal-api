package com.commercepal.apiservice.utils;

import jakarta.servlet.http.HttpServletRequest;

public final class ClientIpUtils {

  private ClientIpUtils() {
    // Prevent instantiation
  }

  /**
   * Extract client IP address from HttpServletRequest considering proxy headers.
   */
  public static String getClientIpAddress(HttpServletRequest request) {
    String[] headers = {
        "X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP",
        "HTTP_X_FORWARDED_FOR", "HTTP_X_FORWARDED", "HTTP_X_CLUSTER_CLIENT_IP",
        "HTTP_CLIENT_IP", "HTTP_FORWARDED_FOR", "HTTP_FORWARDED", "HTTP_VIA",
        "REMOTE_ADDR"
    };

    for (String header : headers) {
      String ip = request.getHeader(header);
      if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
        return ip.split(",")[0].trim();
      }
    }

    return request.getRemoteAddr();
  }
}
