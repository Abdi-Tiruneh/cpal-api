package com.commercepal.apiservice.products;

import com.commercepal.apiservice.shared.enums.Provider;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Metrics service for monitoring product operations performance. Provides real-time insights into
 * search latency, cache hits, errors, etc.
 */
@Slf4j
@Service
public class ProductMetricsService {

  private final Counter searchRequests;
  private final Counter searchErrors;
  private final Counter cacheHits;
  private final Counter cacheMisses;
  private final Counter providerErrors;
  private final Timer searchLatency;
  private final Timer providerLatency;
  private final Timer transformLatency;

  public ProductMetricsService(MeterRegistry meterRegistry) {
    // Request counters
    this.searchRequests = Counter.builder("product.search.requests")
        .description("Total product search requests")
        .tag("service", "product")
        .register(meterRegistry);

    this.searchErrors = Counter.builder("product.search.errors")
        .description("Product search errors")
        .tag("service", "product")
        .register(meterRegistry);

    // Cache metrics
    this.cacheHits = Counter.builder("product.cache.hits")
        .description("Product cache hits")
        .tag("service", "product")
        .register(meterRegistry);

    this.cacheMisses = Counter.builder("product.cache.misses")
        .description("Product cache misses")
        .tag("service", "product")
        .register(meterRegistry);

    // Provider metrics
    this.providerErrors = Counter.builder("product.provider.errors")
        .description("External provider errors")
        .tag("service", "product")
        .register(meterRegistry);

    // Latency timers
    this.searchLatency = Timer.builder("product.search.latency")
        .description("Product search latency")
        .tag("service", "product")
        .register(meterRegistry);

    this.providerLatency = Timer.builder("product.provider.latency")
        .description("External provider call latency")
        .tag("service", "product")
        .register(meterRegistry);

    this.transformLatency = Timer.builder("product.transform.latency")
        .description("Product transformation latency")
        .tag("service", "product")
        .register(meterRegistry);

    log.info("Product metrics service initialized");
  }

  public void incrementSearchRequests() {
    searchRequests.increment();
  }

  public void incrementSearchErrors() {
    searchErrors.increment();
  }

  public void incrementCacheHits() {
    cacheHits.increment();
  }

  public void incrementCacheMisses() {
    cacheMisses.increment();
  }

  public void incrementProviderErrors(Provider provider) {
    providerErrors.increment();
    log.warn("Provider error: {}", provider);
  }

  public void recordSearchLatency(long durationMs) {
    searchLatency.record(durationMs, TimeUnit.MILLISECONDS);
  }

  public void recordProviderLatency(Provider provider, long durationMs) {
    providerLatency.record(durationMs, TimeUnit.MILLISECONDS);
    log.debug("Provider {} latency: {}ms", provider, durationMs);
  }

  public void recordTransformLatency(long durationMs) {
    transformLatency.record(durationMs, TimeUnit.MILLISECONDS);
  }

  public Timer.Sample startTimer() {
    return Timer.start();
  }

  public void stopTimer(Timer.Sample sample, Timer timer) {
    sample.stop(timer);
  }
}

