package com.commercepal.apiservice.shared.logging;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * Performance Monitoring Logger
 * <p>
 * Features: - Method execution time tracking - Database query performance monitoring - External API
 * call tracking - Memory usage monitoring - Thread pool statistics - Custom performance metrics
 */
@Component
@Slf4j(topic = "com.fastpay.agent.performance")
public class PerformanceLogger {

  private static final Map<String, PerformanceMetrics> metricsCache = new ConcurrentHashMap<>();

  /**
   * Track method execution time
   */
  public <T> T trackExecution(String operationName, PerformanceOperation<T> operation) {
    Instant startTime = Instant.now();

    try {
      T result = operation.execute();
      long duration = Duration.between(startTime, Instant.now()).toMillis();

      logPerformance(operationName, duration, true, null);
      updateMetrics(operationName, duration, true);

      return result;
    } catch (Exception e) {
      long duration = Duration.between(startTime, Instant.now()).toMillis();

      logPerformance(operationName, duration, false, e.getMessage());
      updateMetrics(operationName, duration, false);

      throw new RuntimeException("Performance tracked operation failed: " + operationName, e);
    }
  }

  /**
   * Track database query performance
   */
  public void trackDatabaseQuery(String queryName, long duration, int rowCount) {
    MDC.put("queryName", queryName);
    MDC.put("duration", String.valueOf(duration));
    MDC.put("rowCount", String.valueOf(rowCount));

    if (duration > 1000) {
      log.warn("SLOW DATABASE QUERY: {} took {} ms and returned {} rows",
          queryName, duration, rowCount);
    } else {
      log.debug("Database query: {} completed in {} ms with {} rows",
          queryName, duration, rowCount);
    }

    MDC.remove("queryName");
    MDC.remove("duration");
    MDC.remove("rowCount");
  }

  /**
   * Track external API call performance
   */
  public void trackExternalApiCall(String apiName, String endpoint, long duration, int statusCode) {
    MDC.put("apiName", apiName);
    MDC.put("endpoint", endpoint);
    MDC.put("duration", String.valueOf(duration));
    MDC.put("statusCode", String.valueOf(statusCode));

    if (statusCode >= 500) {
      log.error("EXTERNAL API ERROR: {} {} returned {} in {} ms",
          apiName, endpoint, statusCode, duration);
    } else if (statusCode >= 400) {
      log.warn("EXTERNAL API CLIENT ERROR: {} {} returned {} in {} ms",
          apiName, endpoint, statusCode, duration);
    } else if (duration > 5000) {
      log.warn("SLOW EXTERNAL API CALL: {} {} took {} ms",
          apiName, endpoint, duration);
    } else {
      log.info("External API call: {} {} completed in {} ms with status {}",
          apiName, endpoint, duration, statusCode);
    }

    MDC.remove("apiName");
    MDC.remove("endpoint");
    MDC.remove("duration");
    MDC.remove("statusCode");
  }

  /**
   * Log memory usage
   */
  public void logMemoryUsage() {
    Runtime runtime = Runtime.getRuntime();
    long totalMemory = runtime.totalMemory();
    long freeMemory = runtime.freeMemory();
    long usedMemory = totalMemory - freeMemory;
    long maxMemory = runtime.maxMemory();

    double usedPercentage = (usedMemory * 100.0) / maxMemory;

    log.info("Memory Usage: Used: {} MB / Max: {} MB ({}%)",
        usedMemory / 1024 / 1024,
        maxMemory / 1024 / 1024,
        String.format("%.2f", usedPercentage));

    if (usedPercentage > 80) {
      log.warn("HIGH MEMORY USAGE: {}% of max memory is being used",
          String.format("%.2f", usedPercentage));
    }
  }

  /**
   * Log thread pool statistics
   */
  public void logThreadPoolStats(String poolName, int activeThreads, int poolSize, int queueSize) {
    log.info("Thread Pool [{}]: Active: {} / Size: {} | Queue: {}",
        poolName, activeThreads, poolSize, queueSize);

    if (queueSize > 100) {
      log.warn("HIGH THREAD POOL QUEUE SIZE: {} has {} tasks queued",
          poolName, queueSize);
    }
  }

  /**
   * Log custom performance metric
   */
  public void logCustomMetric(String metricName, long value, String unit) {
    MDC.put("metricName", metricName);
    MDC.put("value", String.valueOf(value));
    MDC.put("unit", unit);

    log.info("Custom Metric: {} = {} {}", metricName, value, unit);

    MDC.remove("metricName");
    MDC.remove("value");
    MDC.remove("unit");
  }

  /**
   * Get performance statistics for an operation
   */
  public PerformanceMetrics getMetrics(String operationName) {
    return metricsCache.get(operationName);
  }

  /**
   * Log all accumulated metrics
   */
  public void logAllMetrics() {
    log.info("╔════════════════════════════════════════════════════════════════════════════════╗");
    log.info("║ PERFORMANCE METRICS SUMMARY                                                    ║");
    log.info("╠════════════════════════════════════════════════════════════════════════════════╣");

    metricsCache.forEach((operation, metrics) -> {
      log.info("║ Operation: {}", operation);
      log.info("║   Total Calls: {} | Success: {} | Failures: {}",
          metrics.getTotalCalls(), metrics.getSuccessCount(), metrics.getFailureCount());
      log.info("║   Avg Duration: {} ms | Min: {} ms | Max: {} ms",
          metrics.getAverageDuration(), metrics.getMinDuration(), metrics.getMaxDuration());
      log.info(
          "╠════════════════════════════════════════════════════════════════════════════════╣");
    });

    log.info("╚════════════════════════════════════════════════════════════════════════════════╝");
  }

  /**
   * Clear all metrics
   */
  public void clearMetrics() {
    metricsCache.clear();
    log.info("Performance metrics cleared");
  }

  private void logPerformance(String operationName, long duration, boolean success, String error) {
    MDC.put("operationName", operationName);
    MDC.put("duration", String.valueOf(duration));

    if (!success) {
      log.error("Operation FAILED: {} took {} ms | Error: {}",
          operationName, duration, error);
    } else if (duration > 1000) {
      log.warn("SLOW OPERATION: {} took {} ms", operationName, duration);
    } else {
      log.debug("Operation completed: {} in {} ms", operationName, duration);
    }

    MDC.remove("operationName");
    MDC.remove("duration");
  }

  private void updateMetrics(String operationName, long duration, boolean success) {
    metricsCache.compute(operationName, (opName, metrics) -> {
      if (metrics == null) {
        metrics = new PerformanceMetrics(opName);
      }
      metrics.recordExecution(duration, success);
      return metrics;
    });
  }

  /**
   * Functional interface for performance tracking
   */
  @FunctionalInterface
  public interface PerformanceOperation<T> {

    T execute() throws Exception;
  }

  /**
   * Performance metrics data class
   */
  public static class PerformanceMetrics {

    @Getter
    private final String operationName;
    @Getter
    private long totalCalls;
    @Getter
    private long successCount;
    @Getter
    private long failureCount;
    private long totalDuration;
    private long minDuration = Long.MAX_VALUE;
    private long maxDuration = Long.MIN_VALUE;

    public PerformanceMetrics(String operationName) {
      this.operationName = operationName;
    }

    public synchronized void recordExecution(long duration, boolean success) {
      totalCalls++;
      if (success) {
        successCount++;
      } else {
        failureCount++;
      }
      totalDuration += duration;
      minDuration = Math.min(minDuration, duration);
      maxDuration = Math.max(maxDuration, duration);
    }

    public long getAverageDuration() {
      return totalCalls > 0 ? totalDuration / totalCalls : 0;
    }

    public long getMinDuration() {
      return minDuration == Long.MAX_VALUE ? 0 : minDuration;
    }

    public long getMaxDuration() {
      return maxDuration == Long.MIN_VALUE ? 0 : maxDuration;
    }
  }
}

