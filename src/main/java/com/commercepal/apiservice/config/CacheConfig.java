package com.commercepal.apiservice.config;

import java.time.Duration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Cache Configuration for E-Commerce Platform
 * <p>
 * Configures Redis-based caching with appropriate TTLs for different cache types. Optimized for
 * high-traffic scenarios with efficient serialization.
 */
@Configuration
@EnableCaching
public class CacheConfig {

  @Bean
  public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
    // Create JSON serializer for Redis cache values
    RedisSerializer<Object> jsonSerializer = RedisSerializer.json();

    RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofHours(1)) // Default 1 hour TTL
        .serializeKeysWith(
            RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
        .serializeValuesWith(
            RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
        .disableCachingNullValues();

    // Product cache - longer TTL as products don't change frequently
    RedisCacheConfiguration productCacheConfig = defaultConfig.entryTtl(Duration.ofHours(6));

    // Category cache - even longer TTL
    RedisCacheConfiguration categoryCacheConfig = defaultConfig.entryTtl(Duration.ofHours(12));

    // User session cache - shorter TTL
    RedisCacheConfiguration sessionCacheConfig = defaultConfig.entryTtl(Duration.ofMinutes(30));

    return RedisCacheManager.builder(redisConnectionFactory)
        .cacheDefaults(defaultConfig)
        .withCacheConfiguration("products", productCacheConfig)
        .withCacheConfiguration("categories", categoryCacheConfig)
        .withCacheConfiguration("sessions", sessionCacheConfig)
        .transactionAware()
        .build();
  }
}

