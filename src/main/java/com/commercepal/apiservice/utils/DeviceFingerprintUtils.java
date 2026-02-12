package com.commercepal.apiservice.utils;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for generating device fingerprints from HTTP requests. Device fingerprints help
 * identify unique devices/browsers for security and tracking purposes.
 */
@Slf4j
public final class DeviceFingerprintUtils {

  private static final String UNKNOWN = "unknown";
  private static final String HASH_ALGORITHM = "SHA-256";

  private DeviceFingerprintUtils() {
    // Prevent instantiation
  }

  /**
   * Extract and generate a unique device fingerprint from the HTTP request. The fingerprint is
   * based on User-Agent, Accept headers, and other browser characteristics.
   *
   * @param request the HTTP servlet request
   * @return a unique device fingerprint hash, or "unknown" if unable to generate
   */
  public static String extractDeviceFingerprint(HttpServletRequest request) {
    if (request == null) {
      log.warn("[DEVICE] Cannot extract fingerprint from null request");
      return UNKNOWN;
    }

    try {
      String userAgent = getHeaderOrDefault(request, "User-Agent");
      String acceptLanguage = getHeaderOrDefault(request, "Accept-Language");
      String acceptEncoding = getHeaderOrDefault(request, "Accept-Encoding");
      String accept = getHeaderOrDefault(request, "Accept");
      String secChUa = getHeaderOrDefault(request, "Sec-CH-UA");
      String secChUaPlatform = getHeaderOrDefault(request, "Sec-CH-UA-Platform");
      String secChUaMobile = getHeaderOrDefault(request, "Sec-CH-UA-Mobile");

      // Combine all characteristics into a single string
      String fingerprintData = Stream.of(
          userAgent,
          acceptLanguage,
          acceptEncoding,
          accept,
          secChUa,
          secChUaPlatform,
          secChUaMobile
      ).reduce("", (a, b) -> a + "|" + b);

      // Generate SHA-256 hash
      String fingerprint = generateHash(fingerprintData);

      log.debug("[DEVICE] Generated fingerprint: {} from UA: {}",
          fingerprint.substring(0, Math.min(16, fingerprint.length())),
          userAgent.substring(0, Math.min(50, userAgent.length())));

      return fingerprint;

    } catch (Exception e) {
      log.error("[DEVICE] Error generating device fingerprint: {}", e.getMessage());
      return UNKNOWN;
    }
  }

  /**
   * Extract a simple device type from the User-Agent string.
   *
   * @param request the HTTP servlet request
   * @return device type (MOBILE, TABLET, DESKTOP, or UNKNOWN)
   */
  public static String extractDeviceType(HttpServletRequest request) {
    if (request == null) {
      return UNKNOWN;
    }

    String userAgent = getHeaderOrDefault(request, "User-Agent").toLowerCase();

    if (userAgent.contains("mobile") || userAgent.contains("android") ||
        userAgent.contains("iphone") || userAgent.contains("ipod")) {
      return "MOBILE";
    } else if (userAgent.contains("tablet") || userAgent.contains("ipad")) {
      return "TABLET";
    } else if (!userAgent.equals(UNKNOWN)) {
      return "DESKTOP";
    }

    return UNKNOWN;
  }

  /**
   * Extract browser name from User-Agent.
   *
   * @param request the HTTP servlet request
   * @return browser name (Chrome, Firefox, Safari, Edge, Opera, or UNKNOWN)
   */
  public static String extractBrowserName(HttpServletRequest request) {
    if (request == null) {
      return UNKNOWN;
    }

    String userAgent = getHeaderOrDefault(request, "User-Agent").toLowerCase();

    if (userAgent.contains("edg/")) {
      return "Edge";
    } else if (userAgent.contains("chrome/") && !userAgent.contains("edg/")) {
      return "Chrome";
    } else if (userAgent.contains("firefox/")) {
      return "Firefox";
    } else if (userAgent.contains("safari/") && !userAgent.contains("chrome/")) {
      return "Safari";
    } else if (userAgent.contains("opera/") || userAgent.contains("opr/")) {
      return "Opera";
    }

    return UNKNOWN;
  }

  /**
   * Extract operating system from User-Agent.
   *
   * @param request the HTTP servlet request
   * @return OS name (Windows, macOS, Linux, Android, iOS, or UNKNOWN)
   */
  public static String extractOperatingSystem(HttpServletRequest request) {
    if (request == null) {
      return UNKNOWN;
    }

    String userAgent = getHeaderOrDefault(request, "User-Agent").toLowerCase();

    if (userAgent.contains("windows")) {
      return "Windows";
    } else if (userAgent.contains("mac os") || userAgent.contains("macos")) {
      return "macOS";
    } else if (userAgent.contains("linux") && !userAgent.contains("android")) {
      return "Linux";
    } else if (userAgent.contains("android")) {
      return "Android";
    } else if (userAgent.contains("iphone") || userAgent.contains("ipad") ||
        userAgent.contains("ipod")) {
      return "iOS";
    }

    return UNKNOWN;
  }

  /**
   * Generate a SHA-256 hash from the input string.
   *
   * @param input the input string to hash
   * @return hexadecimal string representation of the hash
   */
  private static String generateHash(String input) {
    try {
      MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
      byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
      return bytesToHex(hashBytes);
    } catch (NoSuchAlgorithmException e) {
      log.error("[DEVICE] SHA-256 algorithm not available: {}", e.getMessage());
      return UNKNOWN;
    }
  }

  /**
   * Convert byte array to hexadecimal string.
   *
   * @param bytes the byte array to convert
   * @return hexadecimal string representation
   */
  private static String bytesToHex(byte[] bytes) {
    StringBuilder result = new StringBuilder();
    for (byte b : bytes) {
      result.append(String.format("%02x", b));
    }
    return result.toString();
  }

  /**
   * Get header value or return default if not present.
   *
   * @param request    the HTTP servlet request
   * @param headerName the name of the header
   * @return header value or UNKNOWN if not present
   */
  private static String getHeaderOrDefault(HttpServletRequest request, String headerName) {
    String value = request.getHeader(headerName);
    return (value != null && !value.isEmpty()) ? value : UNKNOWN;
  }
}

