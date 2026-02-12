package com.commercepal.apiservice.shared.cache;

import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * In-memory cache service to replace Redis functionality. Provides key-value storage, set
 * operations, and automatic expiration.
 */
@Service
@Slf4j
public class InMemoryCacheService {

  private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, Set<Object>> sets = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, List<Object>> lists = new ConcurrentHashMap<>();
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

  /**
   * Store a value with expiration.
   */
  public void set(String key, Object value, long expirationMs) {
    CacheEntry entry = new CacheEntry(value, System.currentTimeMillis() + expirationMs);
    cache.put(key, entry);

    // Schedule cleanup if not already scheduled
    if (expirationMs > 0) {
      scheduler.schedule(() -> {
        CacheEntry existing = cache.get(key);
        if (existing != null && existing.expiresAt <= System.currentTimeMillis()) {
          cache.remove(key);
        }
      }, expirationMs, TimeUnit.MILLISECONDS);
    }
  }

  /**
   * Get a value by key.
   */
  public Object get(String key) {
    CacheEntry entry = cache.get(key);
    if (entry == null) {
      return null;
    }

    // Check if expired
    if (entry.expiresAt > 0 && entry.expiresAt <= System.currentTimeMillis()) {
      cache.remove(key);
      return null;
    }

    return entry.value;
  }

  /**
   * Check if a key exists.
   */
  public boolean hasKey(String key) {
    CacheEntry entry = cache.get(key);
    if (entry == null) {
      return false;
    }

    // Check if expired
    if (entry.expiresAt > 0 && entry.expiresAt <= System.currentTimeMillis()) {
      cache.remove(key);
      return false;
    }

    return true;
  }

  /**
   * Delete a key.
   */
  public void delete(String key) {
    cache.remove(key);
    sets.remove(key);
    lists.remove(key);
  }

  /**
   * Add a member to a set.
   */
  public void addToSet(String key, Object value) {
    sets.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet()).add(value);
  }

  /**
   * Get all members of a set.
   */
  public Set<Object> getSetMembers(String key) {
    Set<Object> set = sets.get(key);
    return set != null ? new HashSet<>(set) : new HashSet<>();
  }

  /**
   * Remove a member from a set.
   */
  public void removeFromSet(String key, Object value) {
    Set<Object> set = sets.get(key);
    if (set != null) {
      set.remove(value);
      if (set.isEmpty()) {
        sets.remove(key);
      }
    }
  }

  /**
   * Set expiration for a key.
   */
  public void expire(String key, long expirationMs) {
    CacheEntry entry = cache.get(key);
    if (entry != null) {
      entry.expiresAt = System.currentTimeMillis() + expirationMs;
    }

    // Also handle set expiration by storing expiration info
    if (sets.containsKey(key)) {
      scheduler.schedule(() -> {
        sets.remove(key);
      }, expirationMs, TimeUnit.MILLISECONDS);
    }

    // Handle list expiration
    if (lists.containsKey(key)) {
      scheduler.schedule(() -> {
        lists.remove(key);
      }, expirationMs, TimeUnit.MILLISECONDS);
    }
  }

  /**
   * Set expiration for a key using Duration.
   */
  public void expire(String key, Duration duration) {
    expire(key, duration.toMillis());
  }

  /**
   * Store a value with expiration using Duration.
   */
  public void set(String key, Object value, Duration duration) {
    set(key, value, duration.toMillis());
  }

  /**
   * Increment a counter value. If key doesn't exist, creates it with value 1.
   */
  public Long increment(String key) {
    return increment(key, 1);
  }

  /**
   * Increment a counter value by specified amount.
   */
  public Long increment(String key, long delta) {
    CacheEntry entry = cache.computeIfAbsent(key, k -> {
      AtomicLong counter = new AtomicLong(0);
      return new CacheEntry(counter, 0); // No expiration for counters
    });

    if (entry.value instanceof AtomicLong counter) {
      return counter.addAndGet(delta);
    } else if (entry.value instanceof Number number) {
      long newValue = number.longValue() + delta;
      entry.value = new AtomicLong(newValue);
      return newValue;
    } else {
      // Convert to AtomicLong
      entry.value = new AtomicLong(delta);
      return delta;
    }
  }

  /**
   * Get counter value as Long.
   */
  public Long getCounter(String key) {
    Object value = get(key);
    if (value == null) {
      return 0L;
    }
    if (value instanceof AtomicLong counter) {
      return counter.get();
    }
    if (value instanceof Number number) {
      return number.longValue();
    }
    return 0L;
  }

  /**
   * Add an element to the right (end) of a list.
   */
  public Long rightPush(String key, Object value) {
    List<Object> list = lists.computeIfAbsent(key, k -> new LinkedList<>());
    list.add(value);
    return (long) list.size();
  }

  /**
   * Get list size.
   */
  public Long listSize(String key) {
    List<Object> list = lists.get(key);
    return list != null ? (long) list.size() : 0L;
  }

  /**
   * Shutdown scheduler on application shutdown.
   */
  @PreDestroy
  public void shutdown() {
    scheduler.shutdown();
    try {
      if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
        scheduler.shutdownNow();
      }
    } catch (InterruptedException e) {
      scheduler.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }


  /**
   * Internal cache entry with expiration.
   */
  private static class CacheEntry {

    Object value;
    long expiresAt;

    CacheEntry(Object value, long expiresAt) {
      this.value = value;
      this.expiresAt = expiresAt;
    }
  }
}

