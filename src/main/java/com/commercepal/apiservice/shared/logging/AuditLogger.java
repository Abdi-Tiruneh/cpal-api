package com.commercepal.apiservice.shared.logging;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * Security and Business Audit Logger
 * <p>
 * Features: - Security event logging (authentication, authorization, access control) - Business
 * event logging (payments, refunds) - Compliance logging (GDPR, PCI-DSS) - User activity tracking -
 * Admin action logging - Data modification tracking
 */
@Component
@Slf4j(topic = "com.fastpay.agent.security")
public class AuditLogger {

  /**
   * Log authentication events
   */
  public void logAuthentication(String userId, String action, boolean success, String ipAddress,
      String details) {
    MDC.put("userId", userId);
    MDC.put("action", action);
    MDC.put("result", success ? "SUCCESS" : "FAILURE");
    MDC.put("ipAddress", ipAddress);

    if (success) {
      log.info("AUTHENTICATION SUCCESS: User {} performed {} from {}", userId, action, ipAddress);
    } else {
      log.warn("AUTHENTICATION FAILURE: User {} failed {} from {} | Details: {}",
          userId, action, ipAddress, details);
    }

    clearMDC();
  }

  /**
   * Log authorization events
   */
  public void logAuthorization(String userId, String resource, String action, boolean granted,
      String reason) {
    MDC.put("userId", userId);
    MDC.put("resource", resource);
    MDC.put("action", action);
    MDC.put("result", granted ? "GRANTED" : "DENIED");

    if (granted) {
      log.info("✓ AUTHORIZATION GRANTED: User {} can {} on {}", userId, action, resource);
    } else {
      log.warn("AUTHORIZATION DENIED: User {} cannot {} on {} | Reason: {}",
          userId, action, resource, reason);
    }

    clearMDC();
  }

  /**
   * Log payment transaction events
   */
  public void logPaymentTransaction(String transactionId, String orderId, String customerId,
      Double amount, String currency, String paymentMethod,
      String gateway, String status, String details) {
    MDC.put("transactionId", transactionId);
    MDC.put("orderId", orderId);
    MDC.put("customerId", customerId);
    MDC.put("amount", String.valueOf(amount));
    MDC.put("currency", currency);
    MDC.put("paymentMethod", paymentMethod);
    MDC.put("gateway", gateway);
    MDC.put("paymentStatus", status);

    log.info(
        "PAYMENT TRANSACTION: {} | Transaction: {} | Order: {} | Amount: {} {} | Method: {} | Gateway: {} | Status: {}",
        status, transactionId, orderId, amount, currency, paymentMethod, gateway, status);

    if (details != null && !details.isEmpty()) {
      log.debug("Payment details: {}", details);
    }

    // Alert on failed payments
    if ("FAILED".equalsIgnoreCase(status) || "DECLINED".equalsIgnoreCase(status)) {
      log.error("PAYMENT FAILED: Transaction {} for order {} failed | Details: {}",
          transactionId, orderId, details);
    }

    clearMDC();
  }

  /**
   * Log refund events
   */
  public void logRefund(String refundId, String transactionId, String orderId,
      Double amount, String currency, String reason, String initiatedBy) {
    MDC.put("refundId", refundId);
    MDC.put("transactionId", transactionId);
    MDC.put("orderId", orderId);
    MDC.put("amount", String.valueOf(amount));
    MDC.put("currency", currency);

    log.warn(
        "REFUND INITIATED: Refund {} for transaction {} | Amount: {} {} | Reason: {} | Initiated by: {}",
        refundId, transactionId, amount, currency, reason, initiatedBy);

    clearMDC();
  }

  /**
   * Log user activity
   */
  public void logUserActivity(String userId, String activity, String resource,
      Map<String, Object> metadata) {
    MDC.put("userId", userId);
    MDC.put("action", activity);
    MDC.put("resource", resource);

    log.info("USER ACTIVITY: User {} performed {} on {}", userId, activity, resource);

    if (metadata != null && !metadata.isEmpty()) {
      log.debug("Activity metadata: {}", metadata);
    }

    clearMDC();
  }

  /**
   * Log admin actions
   */
  public void logAdminAction(String adminId, String action, String targetResource,
      String targetId, String reason) {
    MDC.put("userId", adminId);
    MDC.put("action", action);
    MDC.put("resource", targetResource);

    log.warn("ADMIN ACTION: Admin {} performed {} on {} (ID: {}) | Reason: {}",
        adminId, action, targetResource, targetId, reason);

    clearMDC();
  }

  /**
   * Log data modification events
   */
  public void logDataModification(String userId, String entity, String entityId,
      String operation, Map<String, Object> oldValues,
      Map<String, Object> newValues) {
    MDC.put("userId", userId);
    MDC.put("entity", entity);
    MDC.put("operation", operation);

    log.info("DATA MODIFICATION: User {} performed {} on {} (ID: {})",
        userId, operation, entity, entityId);

    if (log.isDebugEnabled()) {
      log.debug("Old values: {}", oldValues);
      log.debug("New values: {}", newValues);
    }

    clearMDC();
  }

  /**
   * Log security incidents
   */
  public void logSecurityIncident(String incidentType, String userId, String ipAddress,
      String details, String severity) {
    MDC.put("userId", userId);
    MDC.put("ipAddress", ipAddress);
    MDC.put("incidentType", incidentType);

    log.error("SECURITY INCIDENT [{}]: {} | User: {} | IP: {} | Details: {}",
        severity, incidentType, userId, ipAddress, details);

    clearMDC();
  }

  /**
   * Log GDPR compliance events
   */
  public void logGDPREvent(String userId, String eventType, String dataType, String action) {
    MDC.put("userId", userId);
    MDC.put("eventType", eventType);
    MDC.put("dataType", dataType);
    MDC.put("action", action);

    log.info("GDPR EVENT: {} | User: {} | Data Type: {} | Action: {}",
        eventType, userId, dataType, action);

    clearMDC();
  }

  /**
   * Log PCI-DSS compliance events
   */
  public void logPCIDSSEvent(String eventType, String userId, String action, String details) {
    MDC.put("userId", userId);
    MDC.put("eventType", eventType);
    MDC.put("action", action);

    log.info("PCI-DSS EVENT: {} | User: {} | Action: {} | Details: {}",
        eventType, userId, action, details);

    clearMDC();
  }

  /**
   * Log inventory changes
   */
  public void logInventoryChange(String productId, String sku, int oldQuantity,
      int newQuantity, String reason, String userId) {
    MDC.put("productId", productId);
    MDC.put("userId", userId);

    int change = newQuantity - oldQuantity;
    String changeSymbol = change > 0 ? "+" : "";

    log.info("INVENTORY CHANGE: Product {} (SKU: {}) | {} {} -> {} ({}{}) | Reason: {} | By: {}",
        productId, sku, oldQuantity, newQuantity, changeSymbol, change, reason, userId);

    // Alert on low stock
    if (newQuantity < 10) {
      log.warn("LOW STOCK ALERT: Product {} (SKU: {}) has only {} units remaining",
          productId, sku, newQuantity);
    }

    clearMDC();
  }

  /**
   * Log price changes
   */
  public void logPriceChange(String productId, String sku, Double oldPrice, Double newPrice,
      String currency, String reason, String userId) {
    MDC.put("productId", productId);
    MDC.put("userId", userId);

    double changePercent = ((newPrice - oldPrice) / oldPrice) * 100;

    log.info(
        "PRICE CHANGE: Product {} (SKU: {}) | {} {} -> {} {} ({}{:.2f}%) | Reason: {} | By: {}",
        productId, sku, oldPrice, currency, newPrice, currency,
        changePercent > 0 ? "+" : "", changePercent, reason, userId);

    clearMDC();
  }

  /**
   * Log customer data access
   */
  public void logCustomerDataAccess(String accessedBy, String customerId, String dataType,
      String purpose) {
    MDC.put("userId", accessedBy);
    MDC.put("customerId", customerId);
    MDC.put("dataType", dataType);

    log.info("CUSTOMER DATA ACCESS: User {} accessed {} data for customer {} | Purpose: {}",
        accessedBy, dataType, customerId, purpose);

    clearMDC();
  }

  /**
   * Log API rate limit events
   */
  public void logRateLimitEvent(String userId, String ipAddress, String endpoint,
      int requestCount, int limit) {
    MDC.put("userId", userId);
    MDC.put("ipAddress", ipAddress);
    MDC.put("endpoint", endpoint);

    log.warn("RATE LIMIT: User {} from {} exceeded rate limit on {} | Requests: {} / Limit: {}",
        userId, ipAddress, endpoint, requestCount, limit);

    clearMDC();
  }

  /**
   * Create business event with structured data
   */
  public void logBusinessEvent(String eventType, Map<String, Object> eventData) {
    MDC.put("eventType", eventType);

    if (eventData != null) {
      eventData.forEach((key, value) -> {
        if (value != null) {
          MDC.put(key, value.toString());
        }
      });
    }

    log.info("BUSINESS EVENT: {} | Data: {}", eventType, eventData);

    clearMDC();
  }

  /**
   * Helper method to clear MDC context
   */
  private void clearMDC() {
    MDC.remove("userId");
    MDC.remove("action");
    MDC.remove("result");
    MDC.remove("ipAddress");
    MDC.remove("resource");
    MDC.remove("orderId");
    MDC.remove("customerId");
    MDC.remove("eventType");
    MDC.remove("transactionId");
    MDC.remove("amount");
    MDC.remove("currency");
    MDC.remove("paymentMethod");
    MDC.remove("gateway");
    MDC.remove("paymentStatus");
    MDC.remove("refundId");
    MDC.remove("entity");
    MDC.remove("operation");
    MDC.remove("incidentType");
    MDC.remove("dataType");
    MDC.remove("productId");
    MDC.remove("endpoint");
  }
}

