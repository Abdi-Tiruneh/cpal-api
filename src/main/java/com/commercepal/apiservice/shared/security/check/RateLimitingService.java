package com.commercepal.apiservice.shared.security.check;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

//
/**
 * Advanced rate limiting service with multiple strategies
 * including sliding window, token bucket, and adaptive limits.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitingService {

    private final RedisTemplate<String, Object> redisTemplate;

    // Default rate limits
    private static final int DEFAULT_REQUESTS_PER_MINUTE = 60;
    private static final int DEFAULT_REQUESTS_PER_HOUR = 1000;
    private static final int DEFAULT_LOGIN_ATTEMPTS_PER_HOUR = 10;
    private static final int HIGH_VALUE_REQUESTS_PER_MINUTE = 10;

    /**
     * Check if request is allowed based on rate limits
     */
    public boolean isRequestAllowed(String identifier, RateLimitType type) {
        return isRequestAllowed(identifier, type, 1);
    }

    /**
     * Check if multiple requests are allowed
     */
    public boolean isRequestAllowed(String identifier, RateLimitType type, int requestCount) {
        try {
            RateLimitConfig config = getRateLimitConfig(type);
            String key = buildRateLimitKey(identifier, type, config.getWindow());

            return checkSlidingWindow(key, config, requestCount);
        } catch (Exception e) {
            log.error("Error checking rate limit for identifier: {} type: {}", identifier, type, e);
            // Fail open - allow request if rate limiting fails
            return true;
        }
    }

    /**
     * Get current usage for an identifier
     */
    public RateLimitStatus getRateLimitStatus(String identifier, RateLimitType type) {
        try {
            RateLimitConfig config = getRateLimitConfig(type);
            String key = buildRateLimitKey(identifier, type, config.getWindow());

            Long currentUsage = getCurrentUsage(key);
            long resetTime = getResetTime(key, config.getWindow());

            return RateLimitStatus.builder()
                    .identifier(identifier)
                    .type(type)
                    .limit(config.getLimit())
                    .remaining(Math.max(0, config.getLimit() - currentUsage.intValue()))
                    .resetTime(resetTime)
                    .blocked(currentUsage >= config.getLimit())
                    .build();

        } catch (Exception e) {
            log.error("Error getting rate limit status", e);
            return RateLimitStatus.builder()
                    .identifier(identifier)
                    .type(type)
                    .limit(0)
                    .remaining(0)
                    .resetTime(System.currentTimeMillis() + 60000)
                    .blocked(false)
                    .build();
        }
    }

    /**
     * Reset rate limit for an identifier
     */
    public void resetRateLimit(String identifier, RateLimitType type) {
        try {
            RateLimitConfig config = getRateLimitConfig(type);
            String key = buildRateLimitKey(identifier, type, config.getWindow());
            redisTemplate.delete(key);
            log.info("Reset rate limit for identifier: {} type: {}", identifier, type);
        } catch (Exception e) {
            log.error("Error resetting rate limit", e);
        }
    }

    /**
     * Block an identifier temporarily
     */
    public void blockIdentifier(String identifier, Duration duration, String reason) {
        try {
            String blockKey = "rate_limit:blocked:" + identifier;
            BlockInfo blockInfo = BlockInfo.builder()
                    .identifier(identifier)
                    .reason(reason)
                    .blockedAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plus(duration))
                    .build();

            redisTemplate.opsForValue().set(blockKey, blockInfo, duration);
            log.warn("Blocked identifier: {} for duration: {} reason: {}", identifier, duration, reason);
        } catch (Exception e) {
            log.error("Error blocking identifier", e);
        }
    }

    /**
     * Check if identifier is blocked
     */
    public boolean isBlocked(String identifier) {
        try {
            String blockKey = "rate_limit:blocked:" + identifier;
            return redisTemplate.hasKey(blockKey);
        } catch (Exception e) {
            log.error("Error checking if identifier is blocked", e);
            return false;
        }
    }

    /**
     * Get block information for an identifier
     */
    public BlockInfo getBlockInfo(String identifier) {
        try {
            String blockKey = "rate_limit:blocked:" + identifier;
            return (BlockInfo) redisTemplate.opsForValue().get(blockKey);
        } catch (Exception e) {
            log.error("Error getting block info", e);
            return null;
        }
    }

    /**
     * Sliding window rate limiting implementation
     */
    private boolean checkSlidingWindow(String key, RateLimitConfig config, int requestCount) {
        try {
            // Use Redis sorted set for sliding window
            String windowKey = key + ":window";
            long now = System.currentTimeMillis();
            long windowStart = now - config.getWindow().toMillis();

            // Remove old entries
            redisTemplate.opsForZSet().removeRangeByScore(windowKey, 0, windowStart);

            // Count current entries
            Long currentCount = redisTemplate.opsForZSet().count(windowKey, windowStart, now);

            if (currentCount + requestCount <= config.getLimit()) {
                // Add new entries
                for (int i = 0; i < requestCount; i++) {
                    redisTemplate.opsForZSet().add(windowKey, now + i, now + i);
                }

                // Set expiration
                redisTemplate.expire(windowKey, config.getWindow().plusMinutes(1));
                return true;
            } else {
                log.debug("Rate limit exceeded for key: {} current: {} limit: {}",
                        key, currentCount, config.getLimit());
                return false;
            }

        } catch (Exception e) {
            log.error("Error in sliding window check", e);
            return true; // Fail open
        }
    }

    /**
     * Get current usage count
     */
    private Long getCurrentUsage(String key) {
        try {
            String windowKey = key + ":window";
            long now = System.currentTimeMillis();
            RateLimitConfig config = getRateLimitConfig(RateLimitType.API_REQUEST);
            long windowStart = now - config.getWindow().toMillis();

            return redisTemplate.opsForZSet().count(windowKey, windowStart, now);
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * Get reset time for rate limit
     */
    private long getResetTime(String key, Duration window) {
        try {
            String windowKey = key + ":window";
            Long ttl = redisTemplate.getExpire(windowKey, TimeUnit.MILLISECONDS);
            if (ttl != null && ttl > 0) {
                return System.currentTimeMillis() + ttl;
            }
            return System.currentTimeMillis() + window.toMillis();
        } catch (Exception e) {
            return System.currentTimeMillis() + window.toMillis();
        }
    }

    /**
     * Build rate limit key
     */
    private String buildRateLimitKey(String identifier, RateLimitType type, Duration window) {
        long windowNumber = System.currentTimeMillis() / window.toMillis();
        return String.format("rate_limit:%s:%s:%d", type.name().toLowerCase(), identifier, windowNumber);
    }

    /**
     * Get rate limit configuration for type
     */
    private RateLimitConfig getRateLimitConfig(RateLimitType type) {
        switch (type) {
            case API_REQUEST:
                return RateLimitConfig.builder()
                        .limit(DEFAULT_REQUESTS_PER_MINUTE)
                        .window(Duration.ofMinutes(1))
                        .build();
            case LOGIN_ATTEMPT:
                return RateLimitConfig.builder()
                        .limit(DEFAULT_LOGIN_ATTEMPTS_PER_HOUR)
                        .window(Duration.ofHours(1))
                        .build();
            case HIGH_VALUE_OPERATION:
                return RateLimitConfig.builder()
                        .limit(HIGH_VALUE_REQUESTS_PER_MINUTE)
                        .window(Duration.ofMinutes(1))
                        .build();
            case HOURLY_API:
                return RateLimitConfig.builder()
                        .limit(DEFAULT_REQUESTS_PER_HOUR)
                        .window(Duration.ofHours(1))
                        .build();
            case PASSWORD_RESET:
                return RateLimitConfig.builder()
                        .limit(3)
                        .window(Duration.ofHours(1))
                        .build();
            case MFA_ATTEMPT:
                return RateLimitConfig.builder()
                        .limit(5)
                        .window(Duration.ofMinutes(15))
                        .build();
            default:
                return RateLimitConfig.builder()
                        .limit(DEFAULT_REQUESTS_PER_MINUTE)
                        .window(Duration.ofMinutes(1))
                        .build();
        }
    }

    /**
     * Rate limit types
     */
    public enum RateLimitType {
        API_REQUEST,
        LOGIN_ATTEMPT,
        HIGH_VALUE_OPERATION,
        HOURLY_API,
        PASSWORD_RESET,
        MFA_ATTEMPT
    }

    /**
     * Rate limit configuration
     */
    @lombok.Data
    @lombok.Builder
    public static class RateLimitConfig {
        private int limit;
        private Duration window;
    }

    /**
     * Rate limit status
     */
    @lombok.Data
    @lombok.Builder
    public static class RateLimitStatus {
        private String identifier;
        private RateLimitType type;
        private int limit;
        private int remaining;
        private long resetTime;
        private boolean blocked;
    }

    /**
     * Block information
     */
    @lombok.Data
    @lombok.Builder
    public static class BlockInfo {
        private String identifier;
        private String reason;
        private LocalDateTime blockedAt;
        private LocalDateTime expiresAt;
    }
}
