package com.commercepal.apiservice.products;

import com.commercepal.apiservice.products.dto.ProductCardResponse;
import com.commercepal.apiservice.products.dto.ProductPageRequestDto;
import com.commercepal.apiservice.products.ot.OTProductListService;
import com.commercepal.apiservice.shared.enums.Provider;
import com.commercepal.apiservice.shared.enums.SupportedCountry;
import com.commercepal.apiservice.shared.enums.SupportedCurrency;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Professional provider product service with enterprise-grade resilience.
 * <p>
 * Performance optimizations: - Async parallel provider calls with custom thread pool - Request
 * deduplication to prevent redundant API calls - Multi-layer caching (L1: Caffeine, L2: Redis via
 * Spring Cache) - Circuit breaker pattern for fault tolerance - Rate limiting to prevent provider
 * overload - Bulkhead pattern to isolate failures - Comprehensive metrics and monitoring - Timeout
 * handling with graceful degradation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProviderProductService {

  private final OTProductListService otProductListService;
  private final RequestDeduplicationService deduplicationService;
  private final ProductMetricsService metricsService;

  // Resilience4j components
  private final CircuitBreaker optimizedProviderCircuitBreaker;
  private final RateLimiter optimizedProviderRateLimiter;
  private final Bulkhead optimizedProviderBulkhead;

  // Custom thread pools
  @Qualifier("optimizedProductSearchExecutor")
  private final Executor productSearchExecutor;

  /**
   * Searches products from multiple providers in parallel. Optimized with deduplication, caching,
   * and circuit breaker.
   */
  // @Cacheable(value = "productSearch", key = "#page + '_' + #size + '_' + #query
  // + '_' + #userCountry.code", unless = "#result.isEmpty()")
  public List<ProductCardResponse> getProductsFromProvider(ProductPageRequestDto requestDto,
      SupportedCountry userCountry, SupportedCurrency targetCurrency) {
    metricsService.incrementSearchRequests();
    long startTime = System.currentTimeMillis();

    String deduplicationKey = deduplicationService.buildSearchKey(requestDto.getPageOrDefault(),
        requestDto.getSizeOrDefault(),
        requestDto.toJsonObject() + ":" + userCountry.getCode() + ":"
            + targetCurrency.getCode());

    try {
      // Deduplicate concurrent identical requests
      CompletableFuture<List<ProductCardResponse>> future = deduplicationService.deduplicate(
          deduplicationKey,
          () -> executeSearchAsync(requestDto, userCountry, targetCurrency));

      List<ProductCardResponse> result = future.get();

      long duration = System.currentTimeMillis() - startTime;
      metricsService.recordSearchLatency(duration);
      log.info("Search completed in {}ms for query: {}", duration, requestDto.query());

      return result;

    } catch (Exception e) {
      log.error("Error searching products for query: {}", requestDto.query(), e);
      metricsService.incrementSearchErrors();
      // Return empty list instead of exploding, or rethrow based on preference
      // For search, empty list often better than 500
      return Collections.emptyList();
    }
  }

  /**
   * Executes search across multiple providers in parallel.
   */
  private CompletableFuture<List<ProductCardResponse>> executeSearchAsync(
      ProductPageRequestDto requestDto, SupportedCountry userCountry,
      SupportedCurrency targetCurrency) {
    // Calculate provider size - distribute evenly
    int size = requestDto.getSizeOrDefault();
    final int providerSize = Math.max(size / 3, 1);

    // Define providers
    List<ProviderConfig> providers = List.of(
        new ProviderConfig(Provider.AMAZON, null),
        new ProviderConfig(Provider.SHEIN, null),
        new ProviderConfig(Provider.ALIEXPRESS_SINGAPORE, null));

    // Fetch from all providers in parallel with timeout protection
    List<CompletableFuture<List<ProductCardResponse>>> providerFutures = providers.stream()
        .map(provider -> fetchFromProviderAsync(
            provider, requestDto, providerSize, userCountry, targetCurrency))
        .toList();

    // Wait for all with timeout
    return CompletableFuture.allOf(providerFutures.toArray(new CompletableFuture[0]))
        .orTimeout(8,
            java.util.concurrent.TimeUnit.SECONDS) // Global timeout slightly less than TimeLimiter
        .handle((v, ex) -> {
          if (ex != null) {
            log.warn("Some providers timed out or failed: {}", ex.getMessage());
          }

          // Collect successful results
          return providerFutures.stream()
              .filter(f -> f.isDone() && !f.isCompletedExceptionally())
              .map(f -> {
                try {
                  return f.get();
                } catch (Exception e) {
                  return Collections.<ProductCardResponse>emptyList();
                }
              })
              .flatMap(List::stream)
              .collect(Collectors.toList());
        });
  }

  /**
   * Fetches products from a single provider with full resilience protection.
   */
  private CompletableFuture<List<ProductCardResponse>> fetchFromProviderAsync(
      ProviderConfig provider,
      ProductPageRequestDto baseRequest,
      int providerSize,
      SupportedCountry userCountry,
      SupportedCurrency targetCurrency) {
    return CompletableFuture.supplyAsync(() -> {
      long startTime = System.currentTimeMillis();

      try {
        // Apply rate limiter
        RateLimiter.waitForPermission(optimizedProviderRateLimiter);

        // Apply bulkhead and circuit breaker
        // Note: We use decorateSupplier for synchronous execution within the async task
        return Bulkhead.decorateSupplier(
                optimizedProviderBulkhead,
                () -> CircuitBreaker.decorateSupplier(
                    optimizedProviderCircuitBreaker,
                    () -> {
                      ProductPageRequestDto providerRequest = baseRequest
                          .withProvider(provider.type.getCode())
                          .withPageSize(baseRequest.getPageOrDefault(), providerSize);

                      if (provider.categoryId != null) {
                        providerRequest = providerRequest.withCategoryId(provider.categoryId);
                      }

                      List<ProductCardResponse> products = otProductListService.getOtProducts(
                          providerRequest,
                          userCountry, targetCurrency);

                      long duration = System.currentTimeMillis() - startTime;
                      metricsService.recordProviderLatency(provider.type, duration);

                      log.debug("Provider {} returned {} products in {}ms",
                          provider.type, products.size(), duration);

                      return products;
                    }).get())
            .get();

      } catch (Exception e) {
        e.printStackTrace();
        log.error("Error fetching from provider {}: {}",
            provider.type, e.getMessage());
        metricsService.incrementProviderErrors(provider.type);
        return new ArrayList<ProductCardResponse>(); // Graceful degradation
      }
    }, productSearchExecutor);
  }

  /**
   * Internal provider configuration holder.
   */
  private record ProviderConfig(Provider type, String categoryId) {

  }
}
