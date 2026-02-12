package com.commercepal.apiservice.products;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ProductConfig {

  @Bean(name = "optimizedProductSearchExecutor")
  public Executor optimizedProductSearchExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(20);
    executor.setMaxPoolSize(100);
    executor.setQueueCapacity(200);
    executor.setThreadNamePrefix("OptProdSearch-");
    executor.setKeepAliveSeconds(60);
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    executor.initialize();
    return executor;
  }

  @Bean
  public CircuitBreaker optimizedProviderCircuitBreaker(CircuitBreakerRegistry registry) {
    CircuitBreakerConfig config = CircuitBreakerConfig.custom()
        .failureRateThreshold(50)
        .waitDurationInOpenState(Duration.ofMillis(10000))
        .permittedNumberOfCallsInHalfOpenState(3)
        .slidingWindowSize(10)
        .build();
    return registry.circuitBreaker("optimizedProviderFullResilience", config);
  }

  @Bean
  public RateLimiter optimizedProviderRateLimiter(RateLimiterRegistry registry) {
    RateLimiterConfig config = RateLimiterConfig.custom()
        .limitForPeriod(50)
        .limitRefreshPeriod(Duration.ofSeconds(1))
        .timeoutDuration(Duration.ofMillis(500))
        .build();
    return registry.rateLimiter("optimizedProviderRateLimiter", config);
  }

  @Bean
  public Bulkhead optimizedProviderBulkhead(BulkheadRegistry registry) {
    BulkheadConfig config = BulkheadConfig.custom()
        .maxConcurrentCalls(50)
        .maxWaitDuration(Duration.ofMillis(500))
        .build();
    return registry.bulkhead("optimizedProviderBulkhead", config);
  }

  @Bean
  public TimeLimiter optimizedProviderTimeLimiter(TimeLimiterRegistry registry) {
    TimeLimiterConfig config = TimeLimiterConfig.custom()
        .timeoutDuration(Duration.ofSeconds(8))
        .build();
    return registry.timeLimiter("optimizedProviderTimeLimiter", config);
  }
}
