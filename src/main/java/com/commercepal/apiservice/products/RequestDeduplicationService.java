package com.commercepal.apiservice.products;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Request deduplication service to prevent redundant API calls. When multiple identical requests
 * arrive concurrently, only one actual call is made. All requesters wait for and share the same
 * response.
 * <p>
 * This is critical for high-traffic scenarios where users might trigger the same search multiple
 * times (e.g., double-clicks, retries).
 */
@Slf4j
@Service
public class RequestDeduplicationService {

  // Active in-flight requests
  private final ConcurrentHashMap<String, CompletableFuture<?>> inFlightRequests =
      new ConcurrentHashMap<>();

  /**
   * Deduplicates a request by its key. If an identical request is in-flight, returns the existing
   * future. Otherwise, executes the supplier.
   *
   * @param key      Unique identifier for the request (e.g., "search:laptop:page0:size10")
   * @param supplier The actual operation to perform if not deduplicated
   * @return CompletableFuture with the result
   */
  @SuppressWarnings("unchecked")
  public <T> CompletableFuture<T> deduplicate(
      String key,
      java.util.function.Supplier<CompletableFuture<T>> supplier
  ) {
    // Check if request is already in-flight
    CompletableFuture<T> existingFuture =
        (CompletableFuture<T>) inFlightRequests.get(key);

    if (existingFuture != null) {
      log.info("Deduplicating request: {} (joining existing call)", key);
      return existingFuture;
    }

    // Execute new request
    log.debug("Executing new request: {}", key);
    CompletableFuture<T> newFuture = supplier.get()
        .whenComplete((result, error) -> {
          // Remove from in-flight
          inFlightRequests.remove(key);
        });

    // Store in-flight request
    inFlightRequests.put(key, newFuture);
    return newFuture;
  }

  /**
   * Builds a cache key for product search requests.
   */
  public String buildSearchKey(int page, int size, String query) {
    return String.format("search:%s:p%d:s%d",
        query.toLowerCase().trim(), page, size);
  }

  /**
   * Builds a cache key for product detail requests.
   */
  public String buildDetailKey(String productId) {
    return String.format("detail:%s", productId);
  }

  /**
   * Gets current stats for monitoring.
   */
  public String getStats() {
    return String.format(
        "InFlight: %d (cache disabled)",
        inFlightRequests.size()
    );
  }
}

