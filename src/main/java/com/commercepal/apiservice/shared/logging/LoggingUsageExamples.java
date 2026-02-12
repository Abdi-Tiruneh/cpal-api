package com.commercepal.apiservice.shared.logging;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Comprehensive examples demonstrating how to use the logging infrastructure in an e-commerce
 * application.
 * <p>
 * This class shows real-world usage patterns for: - Request/Response logging (automatic via
 * LoggingFilter) - Method execution logging (automatic via LoggingAspect) - Performance monitoring
 * - Security auditing - Business event logging
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoggingUsageExamples {

  private final AuditLogger auditLogger;
  private final PerformanceLogger performanceLogger;

  /**
   * Example 1: User Authentication Logging
   */
  public void exampleUserAuthentication(String userId, String ipAddress, boolean success) {
    if (success) {
      auditLogger.logAuthentication(
          userId,
          "LOGIN",
          true,
          ipAddress,
          "User logged in successfully"
      );
    } else {
      auditLogger.logAuthentication(
          userId,
          "LOGIN",
          false,
          ipAddress,
          "Invalid credentials"
      );
    }
  }

  /**
   * Example 3: Payment Processing Logging
   */
  public void examplePaymentProcessing(String transactionId, String orderId,
      String customerId, Double amount) {
    // Log payment attempt
    log.info("Processing payment for order {}", orderId);

    try {
      // Simulate payment processing
      boolean paymentSuccess = processPayment(transactionId, amount);

      if (paymentSuccess) {
        auditLogger.logPaymentTransaction(
            transactionId,
            orderId,
            customerId,
            amount,
            "USD",
            "CREDIT_CARD",
            "STRIPE",
            "SUCCESS",
            "Payment processed successfully"
        );
      } else {
        auditLogger.logPaymentTransaction(
            transactionId,
            orderId,
            customerId,
            amount,
            "USD",
            "CREDIT_CARD",
            "STRIPE",
            "FAILED",
            "Insufficient funds"
        );
      }
    } catch (Exception e) {
      log.error("Payment processing failed for order {}", orderId, e);
      auditLogger.logPaymentTransaction(
          transactionId,
          orderId,
          customerId,
          amount,
          "USD",
          "CREDIT_CARD",
          "STRIPE",
          "ERROR",
          e.getMessage()
      );
    }
  }

  /**
   * Example 4: Performance Tracking for Database Operations
   */
  public void exampleDatabasePerformanceTracking() {
    String result = performanceLogger.trackExecution(
        "fetchCustomerOrders",
        () -> {
          // Simulate database query
          Thread.sleep(150);
          return "Orders fetched successfully";
        }
    );

    log.info("Database operation result: {}", result);
  }

  /**
   * Example 5: External API Call Logging
   */
  public void exampleExternalApiCall(String orderId) {
    long startTime = System.currentTimeMillis();

    try {
      // Simulate external API call
      int statusCode = callShippingAPI(orderId);
      long duration = System.currentTimeMillis() - startTime;

      performanceLogger.trackExternalApiCall(
          "ShippingAPI",
          "/api/v1/shipments",
          duration,
          statusCode
      );

    } catch (Exception e) {
      long duration = System.currentTimeMillis() - startTime;
      performanceLogger.trackExternalApiCall(
          "ShippingAPI",
          "/api/v1/shipments",
          duration,
          500
      );
      log.error("Shipping API call failed", e);
    }
  }

  /**
   * Example 6: Authorization Logging
   */
  public void exampleAuthorizationCheck(String userId, String resource, String action) {
    boolean hasPermission = checkPermission(userId, resource, action);

    auditLogger.logAuthorization(
        userId,
        resource,
        action,
        hasPermission,
        hasPermission ? "User has required role" : "Insufficient permissions"
    );
  }

  /**
   * Example 7: Refund Processing Logging
   */
  public void exampleRefundProcessing(String refundId, String transactionId,
      String orderId, Double amount, String adminId) {
    auditLogger.logRefund(
        refundId,
        transactionId,
        orderId,
        amount,
        "USD",
        "Customer requested refund - damaged item",
        adminId
    );

    log.info("Refund {} processed for order {}", refundId, orderId);
  }

  /**
   * Example 8: Admin Action Logging
   */
  public void exampleAdminAction(String adminId, String targetUserId) {
    auditLogger.logAdminAction(
        adminId,
        "DISABLE_USER_ACCOUNT",
        "USER",
        targetUserId,
        "Multiple failed login attempts detected"
    );

    log.warn("Admin {} disabled user account {}", adminId, targetUserId);
  }

  /**
   * Example 9: Inventory Change Logging
   */
  public void exampleInventoryUpdate(String productId, String sku, int oldQty, int newQty) {
    auditLogger.logInventoryChange(
        productId,
        sku,
        oldQty,
        newQty,
        "ORDER_FULFILLMENT",
        "SYSTEM"
    );
  }

  /**
   * Example 10: Price Change Logging
   */
  public void examplePriceChange(String productId, String sku, Double oldPrice,
      Double newPrice, String adminId) {
    auditLogger.logPriceChange(
        productId,
        sku,
        oldPrice,
        newPrice,
        "USD",
        "Seasonal discount applied",
        adminId
    );
  }

  /**
   * Example 11: Security Incident Logging
   */
  public void exampleSecurityIncident(String userId, String ipAddress) {
    auditLogger.logSecurityIncident(
        "BRUTE_FORCE_ATTACK",
        userId,
        ipAddress,
        "10 failed login attempts in 2 minutes",
        "HIGH"
    );

    log.error("Security incident detected for user {} from IP {}", userId, ipAddress);
  }

  /**
   * Example 12: GDPR Compliance Logging
   */
  public void exampleGDPREvent(String userId) {
    auditLogger.logGDPREvent(
        userId,
        "DATA_EXPORT_REQUEST",
        "PERSONAL_DATA",
        "EXPORT"
    );

    log.info("GDPR data export request logged for user {}", userId);
  }

  /**
   * Example 13: Customer Data Access Logging
   */
  public void exampleCustomerDataAccess(String adminId, String customerId) {
    auditLogger.logCustomerDataAccess(
        adminId,
        customerId,
        "PAYMENT_INFORMATION",
        "Customer support inquiry"
    );
  }

  /**
   * Example 14: Rate Limit Event Logging
   */
  public void exampleRateLimitEvent(String userId, String ipAddress, String endpoint) {
    auditLogger.logRateLimitEvent(
        userId,
        ipAddress,
        endpoint,
        105,
        100
    );
  }

  /**
   * Example 15: Business Event Logging
   */
  public void exampleBusinessEvent() {
    Map<String, Object> eventData = new HashMap<>();
    eventData.put("orderId", "ORD-12345");
    eventData.put("customerId", "CUST-67890");
    eventData.put("productId", "PROD-11111");
    eventData.put("quantity", 2);
    eventData.put("revenue", 199.99);

    auditLogger.logBusinessEvent("PRODUCT_PURCHASED", eventData);
  }

  /**
   * Example 16: Performance Metrics Logging
   */
  public void examplePerformanceMetrics() {
    // Log memory usage
    performanceLogger.logMemoryUsage();

    // Log custom metric
    performanceLogger.logCustomMetric("active_users", 1250, "users");

    // Log all accumulated metrics
    performanceLogger.logAllMetrics();
  }

  /**
   * Example 17: Database Query Performance
   */
  public void exampleDatabaseQueryPerformance() {
    long startTime = System.currentTimeMillis();

    // Simulate database query
    int rowCount = executeQuery();

    long duration = System.currentTimeMillis() - startTime;

    performanceLogger.trackDatabaseQuery(
        "SELECT_CUSTOMER_ORDERS",
        duration,
        rowCount
    );
  }

  /**
   * Example 18: User Activity Logging
   */
  public void exampleUserActivity(String userId, String productId) {
    Map<String, Object> metadata = new HashMap<>();
    metadata.put("productId", productId);
    metadata.put("action", "VIEW");
    metadata.put("timestamp", System.currentTimeMillis());

    auditLogger.logUserActivity(
        userId,
        "VIEW_PRODUCT",
        "PRODUCT",
        metadata
    );
  }

  /**
   * Example 19: Data Modification Logging
   */
  public void exampleDataModification(String userId, String customerId) {
    Map<String, Object> oldValues = new HashMap<>();
    oldValues.put("email", "old@example.com");
    oldValues.put("phone", "+1234567890");

    Map<String, Object> newValues = new HashMap<>();
    newValues.put("email", "new@example.com");
    newValues.put("phone", "+0987654321");

    auditLogger.logDataModification(
        userId,
        "CUSTOMER",
        customerId,
        "UPDATE",
        oldValues,
        newValues
    );
  }

  /**
   * Example 20: Structured Logging with MDC
   */
  public void exampleStructuredLogging(String orderId, String customerId) {
    // MDC is automatically populated by LoggingFilter and LoggingAspect
    // You can also add custom MDC values

    log.info("Processing order for customer");
    log.debug("Order details: orderId={}, customerId={}", orderId, customerId);
    log.warn("Order processing is taking longer than expected");
  }

  // Helper methods for simulation
  private boolean processPayment(String transactionId, Double amount) {
    return true; // Simulate successful payment
  }

  private int callShippingAPI(String orderId) {
    return 200; // Simulate successful API call
  }

  private boolean checkPermission(String userId, String resource, String action) {
    return true; // Simulate permission check
  }

  private int executeQuery() {
    return 42; // Simulate query result
  }
}

