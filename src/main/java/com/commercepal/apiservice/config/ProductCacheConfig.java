package com.commercepal.apiservice.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Multi-layer caching configuration for ultra-fast product operations. L1 Cache: Caffeine
 * (in-memory, sub-millisecond) L2 Cache: Redis (distributed, cross-instance)
 */
@Slf4j
@Configuration
@EnableCaching
public class ProductCacheConfig {

  /**
   * L1 Cache: Local in-memory cache using Caffeine. Ultra-fast access for hot data with automatic
   * eviction.
   */
  @Bean
  @Primary
  public CacheManager caffeineCacheManager() {
    CaffeineCacheManager cacheManager = new CaffeineCacheManager(
        "productSearch", // Search results cache
        "productDetail", // Individual product details (legacy)
        "productDetails", // Product details for display (with reviews/recommendations)
        "productDetailsForOrder", // Product details for order placement (optimized)
        "categoryLookup", // Category metadata
        "brandLookup", // Brand metadata
        "exchangeRates" // Currency exchange rates
    );

    cacheManager.setCaffeine(Caffeine.newBuilder()
        .maximumSize(10_000) // Max 10k entries
        .expireAfterWrite(Duration.ofMinutes(5)) // Refresh every 5 minutes
        .expireAfterAccess(Duration.ofMinutes(3)) // Evict if not accessed
        .recordStats() // Enable metrics
    );

    log.info("Caffeine L1 cache initialized with max size: 10,000, TTL: 5min");
    return cacheManager;
  }

  /**
   * L2 Cache: Distributed Redis cache for cross-instance consistency.
   */
  @Bean
  public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
    RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(15))
        .serializeKeysWith(
            RedisSerializationContext.SerializationPair.fromSerializer(
                new StringRedisSerializer()))
        .serializeValuesWith(
            RedisSerializationContext.SerializationPair.fromSerializer(
                new GenericJackson2JsonRedisSerializer()))
        .disableCachingNullValues();

    // Custom TTL for different cache regions
    RedisCacheConfiguration searchConfig = defaultConfig
        .entryTtl(Duration.ofMinutes(10));

    RedisCacheConfiguration detailConfig = defaultConfig
        .entryTtl(Duration.ofMinutes(30));

    RedisCacheConfiguration metadataConfig = defaultConfig
        .entryTtl(Duration.ofHours(1));

    RedisCacheManager cacheManager = RedisCacheManager.builder(connectionFactory)
        .cacheDefaults(defaultConfig)
        .withCacheConfiguration("productSearchRedis", searchConfig)
        .withCacheConfiguration("productDetailRedis", detailConfig)
        .withCacheConfiguration("productDetails", detailConfig) // Product details for display
        .withCacheConfiguration("productDetailsForOrder",
            detailConfig) // Product details for orders
        .withCacheConfiguration("categoryLookupRedis", metadataConfig)
        .withCacheConfiguration("brandLookupRedis", metadataConfig)
        .withCacheConfiguration("exchangeRatesRedis", metadataConfig)
        .transactionAware()
        .build();

    log.info("Redis L2 cache initialized with distributed support");
    return cacheManager;
  }
}
