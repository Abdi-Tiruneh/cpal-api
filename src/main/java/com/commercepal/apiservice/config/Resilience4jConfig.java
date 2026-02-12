package com.commercepal.apiservice.config;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Resilience4j configuration for fault tolerance and stability. Implements Circuit Breaker, Rate
 * Limiter, Bulkhead, and Time Limiter patterns.
 */
@Slf4j
@Configuration
public class Resilience4jConfig {

  /**
   * Circuit Breaker for external provider API calls. Prevents cascading failures when provider is
   * down.
   */
  @Bean
  public CircuitBreaker providerCircuitBreaker() {
    CircuitBreakerConfig config = CircuitBreakerConfig.custom()
        .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
        .slidingWindowSize(10)                    // Monitor last 10 calls
        .failureRateThreshold(50)                 // Open circuit if 50% fail
        .slowCallRateThreshold(60)                // Open if 60% are slow
        .slowCallDurationThreshold(Duration.ofSeconds(5))  // Define "slow"
        .waitDurationInOpenState(Duration.ofSeconds(30))   // Wait before retry
        .permittedNumberOfCallsInHalfOpenState(3) // Test calls before closing
        .minimumNumberOfCalls(5)                  // Min calls before calculating
        .automaticTransitionFromOpenToHalfOpenEnabled(true)
        .recordExceptions(
            // Exceptions that count as failures
            Exception.class
        )
        .build();

    CircuitBreaker circuitBreaker = CircuitBreaker.of("providerApi", config);

    // Log circuit breaker state changes
    circuitBreaker.getEventPublisher()
        .onStateTransition(event ->
            log.warn("Circuit Breaker state changed: {} -> {}",
                event.getStateTransition().getFromState(),
                event.getStateTransition().getToState())
        )
        .onError(event ->
            log.error("Circuit Breaker recorded error: {}",
                event.getThrowable().getMessage())
        );

    log.info("Provider Circuit Breaker initialized");
    return circuitBreaker;
  }

  /**
   * Rate Limiter to prevent overwhelming external providers.
   */
  @Bean
  public RateLimiter providerRateLimiter() {
    RateLimiterConfig config = RateLimiterConfig.custom()
        .limitForPeriod(100)                      // Max 100 calls
        .limitRefreshPeriod(Duration.ofSeconds(1)) // Per second
        .timeoutDuration(Duration.ofSeconds(2))    // Wait max 2s for permit
        .build();

    RateLimiter rateLimiter = RateLimiter.of("providerApi", config);

    rateLimiter.getEventPublisher()
        .onSuccess(event ->
            log.debug("Rate limiter: {} permits available",
                rateLimiter.getMetrics().getAvailablePermissions())
        );

    log.info("Provider Rate Limiter initialized: 100 calls/second");
    return rateLimiter;
  }

  /**
   * Bulkhead to limit concurrent calls to external providers. Prevents thread pool exhaustion.
   */
  @Bean
  public Bulkhead providerBulkhead() {
    BulkheadConfig config = BulkheadConfig.custom()
        .maxConcurrentCalls(50)                   // Max 50 concurrent calls
        .maxWaitDuration(Duration.ofMillis(500))  // Wait max 500ms for slot
        .build();

    Bulkhead bulkhead = Bulkhead.of("providerApi", config);

    bulkhead.getEventPublisher()
        .onCallPermitted(event ->
            log.debug("Bulkhead: {} concurrent calls",
                bulkhead.getMetrics().getAvailableConcurrentCalls())
        )
        .onCallRejected(event ->
            log.warn("Bulkhead rejected call - max concurrent calls reached")
        );

    log.info("Provider Bulkhead initialized: max 50 concurrent calls");
    return bulkhead;
  }

  /**
   * Time Limiter for enforcing timeouts on async operations.
   */
  @Bean
  public TimeLimiter providerTimeLimiter() {
    TimeLimiterConfig config = TimeLimiterConfig.custom()
        .timeoutDuration(Duration.ofSeconds(10))  // 10s timeout for provider calls
        .cancelRunningFuture(true)                // Cancel on timeout
        .build();

    TimeLimiter timeLimiter = TimeLimiter.of("providerApi", config);

    log.info("Provider Time Limiter initialized: 10s timeout");
    return timeLimiter;
  }
}

