package com.commercepal.apiservice.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * High-performance async configuration for parallel provider calls. Optimized for multiple
 * concurrent external API requests.
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfiguration implements AsyncConfigurer {

  /**
   * Main async executor for product search operations. Configured for high throughput with multiple
   * providers.
   */
  @Bean(name = "productSearchExecutor")
  public Executor productSearchExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

    // Core pool for handling base load (3 providers * multiple concurrent searches)
    executor.setCorePoolSize(20);

    // Max pool for handling peak traffic
    executor.setMaxPoolSize(50);

    // Queue capacity before rejection
    executor.setQueueCapacity(200);

    // Thread naming for easy debugging
    executor.setThreadNamePrefix("product-search-");

    // Caller-runs policy: if queue is full, execute in caller's thread (backpressure)
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

    // Allow core threads to timeout when idle
    executor.setAllowCoreThreadTimeOut(true);
    executor.setKeepAliveSeconds(60);

    // Wait for tasks to complete on shutdown
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(30);

    executor.initialize();

    log.info("Product search executor initialized with core={}, max={}, queue={}",
        executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());

    return executor;
  }

  /**
   * Dedicated executor for HTTP calls to external providers. Prevents thread starvation from
   * blocking I/O operations.
   */
  @Bean(name = "providerHttpExecutor")
  public Executor providerHttpExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

    // Higher pool size for I/O-bound operations
    executor.setCorePoolSize(30);
    executor.setMaxPoolSize(100);
    executor.setQueueCapacity(500);
    executor.setThreadNamePrefix("provider-http-");
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    executor.setAllowCoreThreadTimeOut(true);
    executor.setKeepAliveSeconds(60);
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(30);

    executor.initialize();

    log.info("Provider HTTP executor initialized with core={}, max={}, queue={}",
        executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());

    return executor;
  }

  /**
   * Lightweight executor for fast transformations and data processing.
   */
  @Bean(name = "transformExecutor")
  public Executor transformExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

    // Sized based on CPU cores for CPU-bound operations
    int cpuCores = Runtime.getRuntime().availableProcessors();
    executor.setCorePoolSize(cpuCores);
    executor.setMaxPoolSize(cpuCores * 2);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("transform-");
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    executor.setAllowCoreThreadTimeOut(true);
    executor.setKeepAliveSeconds(30);

    executor.initialize();

    log.info("Transform executor initialized with core={}, max={} (CPU cores: {})",
        executor.getCorePoolSize(), executor.getMaxPoolSize(), cpuCores);

    return executor;
  }

  @Override
  public Executor getAsyncExecutor() {
    return productSearchExecutor();
  }
}

