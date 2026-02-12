package com.commercepal.apiservice.shared.security;

import com.commercepal.apiservice.users.enums.UserType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Reads JWT access and refresh token expiration (TTL) per user type from configuration.
 * <ul>
 *   <li>Staff: short-lived (e.g. 1 hour) for access and refresh</li>
 *   <li>Customer: longer-lived (e.g. 1 week)</li>
 *   <li>Other (MERCHANT, AGENT, SYSTEM): default (e.g. 1 day)</li>
 * </ul>
 */
@Component
@ConfigurationProperties(prefix = "jwt.expiration")
@Data
public class JwtTokenExpirationProperties {

  private static final long ONE_HOUR_MS = 3_600_000L;
  private static final long ONE_DAY_MS = 86_400_000L;
  private static final long ONE_WEEK_MS = 604_800_000L;

  /** Staff: short-lived tokens (e.g. 1 hour). */
  private TtlPair staff = new TtlPair(ONE_HOUR_MS, ONE_HOUR_MS);

  /** Customer: longer-lived tokens (e.g. 1 week). */
  private TtlPair customer = new TtlPair(ONE_WEEK_MS, ONE_WEEK_MS);

  /** Other user types (MERCHANT, AGENT, SYSTEM): default (e.g. 1 day). */
  private TtlPair other = new TtlPair(ONE_DAY_MS, ONE_DAY_MS);

  public long getAccessTtlMs(UserType userType) {
    if (userType == null) {
      return other.getAccessTtlMs();
    }
    return switch (userType) {
      case STAFF -> staff.getAccessTtlMs();
      case CUSTOMER -> customer.getAccessTtlMs();
      case MERCHANT, AGENT, SYSTEM -> other.getAccessTtlMs();
    };
  }

  public long getRefreshTtlMs(UserType userType) {
    if (userType == null) {
      return other.getRefreshTtlMs();
    }
    return switch (userType) {
      case STAFF -> staff.getRefreshTtlMs();
      case CUSTOMER -> customer.getRefreshTtlMs();
      case MERCHANT, AGENT, SYSTEM -> other.getRefreshTtlMs();
    };
  }

  @Data
  public static class TtlPair {
    /** Access token TTL in milliseconds. */
    private long accessTtlMs;
    /** Refresh token TTL in milliseconds. */
    private long refreshTtlMs;

    public TtlPair() {}

    public TtlPair(long accessTtlMs, long refreshTtlMs) {
      this.accessTtlMs = accessTtlMs;
      this.refreshTtlMs = refreshTtlMs;
    }
  }
}
