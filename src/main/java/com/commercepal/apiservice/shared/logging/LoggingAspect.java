package com.commercepal.apiservice.shared.logging;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * AOP Logging Aspect
 * <p>
 * Provides automatic logging for: - All REST controllers - All service methods - All repository
 * methods - Method execution time - Method parameters and return values - Exception handling
 */
@Aspect
@Component
@Slf4j
public class LoggingAspect {

  /**
   * Pointcut for all REST controllers
   */
  @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
  public void restControllerPointcut() {
  }

  /**
   * Pointcut for all service classes
   */
  @Pointcut("within(@org.springframework.stereotype.Service *)")
  public void servicePointcut() {
  }

  /**
   * Pointcut for all repository interfaces
   */
  @Pointcut("within(@org.springframework.stereotype.Repository *)")
  public void repositoryPointcut() {
  }

  /**
   * Pointcut for all classes in the application package
   */
  @Pointcut("within(com.fastpay.agent..*)")
  public void applicationPackagePointcut() {
  }

  /**
   * Around advice for REST controllers
   */
  @Around("restControllerPointcut() && applicationPackagePointcut()")
  public Object logAroundController(ProceedingJoinPoint joinPoint) throws Throwable {
    return logMethodExecution(joinPoint, "CONTROLLER");
  }

  /**
   * Around advice for service methods
   */
  @Around("servicePointcut() && applicationPackagePointcut()")
  public Object logAroundService(ProceedingJoinPoint joinPoint) throws Throwable {
    return logMethodExecution(joinPoint, "SERVICE");
  }

  /**
   * Around advice for repository methods
   */
  @Around("repositoryPointcut() && applicationPackagePointcut()")
  public Object logAroundRepository(ProceedingJoinPoint joinPoint) throws Throwable {
    return logMethodExecution(joinPoint, "REPOSITORY");
  }

  /**
   * Core method execution logging logic
   */
  private Object logMethodExecution(ProceedingJoinPoint joinPoint, String layer) throws Throwable {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    String className = signature.getDeclaringType().getSimpleName();
    String methodName = signature.getName();
    String fullMethodName = className + "." + methodName;

    // Add method context to MDC
    MDC.put("layer", layer);
    MDC.put("class", className);
    MDC.put("method", methodName);

    Instant startTime = Instant.now();

    try {
      // Log method entry
      if (log.isDebugEnabled()) {
        Object[] args = joinPoint.getArgs();
        String argsString = formatArguments(args);
        log.debug("→ [{}] Entering: {} with arguments: {}", layer, fullMethodName, argsString);
      }

      // Execute the method
      Object result = joinPoint.proceed();

      // Calculate execution time
      long executionTime = Duration.between(startTime, Instant.now()).toMillis();

      // Log method exit
      if (log.isDebugEnabled()) {
        log.debug("← [{}] Exiting: {} | Duration: {} ms | Result: {}",
            layer, fullMethodName, executionTime, formatResult(result));
      } else {
        log.info("[{}] {} completed in {} ms", layer, fullMethodName, executionTime);
      }

      // Warn on slow methods
      if (executionTime > 1000) {
        log.warn("SLOW {} METHOD: {} took {} ms", layer, fullMethodName, executionTime);
      }

      return result;

    } catch (Exception e) {
      long executionTime = Duration.between(startTime, Instant.now()).toMillis();

      log.error("✗ [{}] Exception in: {} after {} ms | Error: {}",
          layer, fullMethodName, executionTime, e.getMessage());
      log.error("Exception details:", e);

      throw e;
    } finally {
      // Clean up MDC
      MDC.remove("layer");
      MDC.remove("class");
      MDC.remove("method");
    }
  }

  /**
   * Format method arguments for logging (with sensitive data masking)
   */
  private String formatArguments(Object[] args) {
    if (args == null || args.length == 0) {
      return "none";
    }

    return Arrays.stream(args)
        .map(arg -> {
          if (arg == null) {
            return "null";
          }
          String argString = arg.toString();
          // Mask sensitive data
          return maskSensitiveData(argString);
        })
        .collect(Collectors.joining(", "));
  }

  /**
   * Format method result for logging
   */
  private String formatResult(Object result) {
    if (result == null) {
      return "null";
    }

    String resultString = result.toString();

    // Truncate large results
    if (resultString.length() > 200) {
      return resultString.substring(0, 200) + "... (truncated)";
    }

    return maskSensitiveData(resultString);
  }

  /**
   * Mask sensitive data in logs
   */
  private String maskSensitiveData(String data) {
    if (data == null) {
      return null;
    }

    return data
        .replaceAll("password=[^,\\s}]+", "password=***MASKED***")
        .replaceAll("token=[^,\\s}]+", "token=***MASKED***")
        .replaceAll("apiKey=[^,\\s}]+", "apiKey=***MASKED***")
        .replaceAll("secret=[^,\\s}]+", "secret=***MASKED***")
        .replaceAll("creditCard=[^,\\s}]+", "creditCard=***MASKED***")
        .replaceAll("cvv=[^,\\s}]+", "cvv=***MASKED***");
  }
}

