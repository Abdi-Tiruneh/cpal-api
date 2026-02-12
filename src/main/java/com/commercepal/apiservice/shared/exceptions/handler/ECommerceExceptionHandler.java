package com.commercepal.apiservice.shared.exceptions.handler;

import com.commercepal.apiservice.shared.api.ErrorResponse;
import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;
import com.commercepal.apiservice.shared.exceptions.business.BadRequestException;
import com.commercepal.apiservice.shared.exceptions.business.CartException;
import com.commercepal.apiservice.shared.exceptions.business.CartLimitExceededException;
import com.commercepal.apiservice.shared.exceptions.business.CouponExpiredException;
import com.commercepal.apiservice.shared.exceptions.business.CouponUsageLimitExceededException;
import com.commercepal.apiservice.shared.exceptions.business.CustomerException;
import com.commercepal.apiservice.shared.exceptions.business.DiscountException;
import com.commercepal.apiservice.shared.exceptions.business.InsufficientStockException;
import com.commercepal.apiservice.shared.exceptions.business.InvalidCouponException;
import com.commercepal.apiservice.shared.exceptions.business.InvalidShippingAddressException;
import com.commercepal.apiservice.shared.exceptions.business.InventoryException;
import com.commercepal.apiservice.shared.exceptions.business.OrderCancellationException;
import com.commercepal.apiservice.shared.exceptions.business.OrderException;
import com.commercepal.apiservice.shared.exceptions.business.PaymentException;
import com.commercepal.apiservice.shared.exceptions.business.PaymentLimitExceededException;
import com.commercepal.apiservice.shared.exceptions.business.PaymentMethodNotSupportedException;
import com.commercepal.apiservice.shared.exceptions.business.ProductException;
import com.commercepal.apiservice.shared.exceptions.business.ProductOutOfStockException;
import com.commercepal.apiservice.shared.exceptions.business.PurchaseLimitExceededException;
import com.commercepal.apiservice.shared.exceptions.business.ShippingException;
import com.commercepal.apiservice.shared.exceptions.business.ShippingMethodNotAvailableException;
import com.commercepal.apiservice.shared.exceptions.compliance.AgeVerificationException;
import com.commercepal.apiservice.shared.exceptions.compliance.ComplianceException;
import com.commercepal.apiservice.shared.exceptions.compliance.FraudDetectionException;
import com.commercepal.apiservice.shared.exceptions.resource.CustomerBlockedException;
import com.commercepal.apiservice.shared.exceptions.resource.DuplicateResourceException;
import com.commercepal.apiservice.shared.exceptions.resource.ResourceNotFoundException;
import com.commercepal.apiservice.shared.exceptions.security.AccountExpiredException;
import com.commercepal.apiservice.shared.exceptions.security.AccountLockedException;
import com.commercepal.apiservice.shared.exceptions.security.AccountSuspendedException;
import com.commercepal.apiservice.shared.exceptions.security.AuthorizationException;
import com.commercepal.apiservice.shared.exceptions.security.CredentialsExpiredException;
import com.commercepal.apiservice.shared.exceptions.security.ForbiddenException;
import com.commercepal.apiservice.shared.exceptions.security.LoginAttemptsException;
import com.commercepal.apiservice.shared.exceptions.security.UnauthorizedException;
import com.commercepal.apiservice.shared.exceptions.service.NotificationServiceException;
import com.commercepal.apiservice.shared.exceptions.service.PaymentGatewayException;
import com.commercepal.apiservice.shared.exceptions.service.ServiceUnavailableException;
import com.commercepal.apiservice.shared.exceptions.service.ShippingServiceException;
import com.commercepal.apiservice.shared.exceptions.service.SystemMaintenanceException;
import com.commercepal.apiservice.shared.exceptions.service.ThirdPartyServiceException;
import com.commercepal.apiservice.shared.exceptions.transaction.DuplicateTransactionException;
import com.commercepal.apiservice.shared.exceptions.transaction.OrderProcessingException;
import com.commercepal.apiservice.shared.exceptions.transaction.PaymentAuthorizationFailedException;
import com.commercepal.apiservice.shared.exceptions.transaction.PaymentCaptureFailedException;
import com.commercepal.apiservice.shared.exceptions.transaction.PaymentProcessingException;
import com.commercepal.apiservice.shared.exceptions.transaction.RefundProcessingException;
import com.commercepal.apiservice.shared.exceptions.transaction.TransactionException;
import com.commercepal.apiservice.shared.exceptions.transaction.TransactionExpiredException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.ws.client.WebServiceIOException;
import com.commercepal.apiservice.payments.integration.telebirr.exception.SoapCommunicationException;
import java.net.SocketTimeoutException;

/**
 * Comprehensive exception handler for the e-commerce platform. Handles all exceptions and provides
 * consistent, user-friendly error responses.
 */
@RestControllerAdvice
@Slf4j
public class ECommerceExceptionHandler {

  // ==================== VALIDATION EXCEPTIONS ====================

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    Map<String, String> validationErrors = processFieldErrors(ex.getBindingResult());
    ErrorResponse errorResponse = ErrorResponse.validation(
        HttpStatus.BAD_REQUEST.value(),
        "VALIDATION_ERROR",
        "Request validation failed",
        validationErrors,
        request.getRequestURI(),
        request.getMethod()
    );
    return buildErrorResponse(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(BindException.class)
  public ResponseEntity<ErrorResponse> handleBindException(
      BindException ex, HttpServletRequest request) {
    Map<String, String> validationErrors = processFieldErrors(ex.getBindingResult());
    ErrorResponse errorResponse = ErrorResponse.validation(
        HttpStatus.BAD_REQUEST.value(),
        "BINDING_ERROR",
        "Request binding failed",
        validationErrors,
        request.getRequestURI(),
        request.getMethod()
    );
    return buildErrorResponse(errorResponse, HttpStatus.BAD_REQUEST);
  }

  // ==================== E-COMMERCE BUSINESS EXCEPTIONS ====================

  @ExceptionHandler(BaseECommerceException.class)
  public ResponseEntity<ErrorResponse> handleBaseECommerceException(
      BaseECommerceException ex, HttpServletRequest request) {
    HttpStatus status = mapExceptionToHttpStatus(ex);
    ErrorResponse errorResponse = ErrorResponse.detailed(
        status.value(),
        ex.getErrorCode(),
        ex.getMessage(),
        request.getRequestURI(),
        request.getMethod()
    );
    logException(ex, request);
    return buildErrorResponse(errorResponse, status);
  }

  // ==================== PRODUCT EXCEPTIONS ====================

  @ExceptionHandler({ProductException.class, ProductOutOfStockException.class})
  public ResponseEntity<ErrorResponse> handleProductException(
      BaseECommerceException ex, HttpServletRequest request) {
    ErrorResponse errorResponse = ErrorResponse.detailed(
        HttpStatus.BAD_REQUEST.value(),
        ex.getErrorCode(),
        ex.getMessage(),
        request.getRequestURI(),
        request.getMethod()
    );
    logException(ex, request);
    return buildErrorResponse(errorResponse, HttpStatus.BAD_REQUEST);
  }

  // ==================== ORDER EXCEPTIONS ====================

  @ExceptionHandler({OrderException.class, OrderCancellationException.class,
      OrderProcessingException.class})
  public ResponseEntity<ErrorResponse> handleOrderException(
      BaseECommerceException ex, HttpServletRequest request) {
    ErrorResponse errorResponse = ErrorResponse.detailed(
        HttpStatus.BAD_REQUEST.value(),
        ex.getErrorCode(),
        ex.getMessage(),
        request.getRequestURI(),
        request.getMethod()
    );
    logException(ex, request);
    return buildErrorResponse(errorResponse, HttpStatus.BAD_REQUEST);
  }

  // ==================== CART EXCEPTIONS ====================

  @ExceptionHandler({CartException.class, CartLimitExceededException.class})
  public ResponseEntity<ErrorResponse> handleCartException(
      BaseECommerceException ex, HttpServletRequest request) {
    ErrorResponse errorResponse = ErrorResponse.detailed(
        HttpStatus.BAD_REQUEST.value(),
        ex.getErrorCode(),
        ex.getMessage(),
        request.getRequestURI(),
        request.getMethod()
    );
    return buildErrorResponse(errorResponse, HttpStatus.BAD_REQUEST);
  }

  // ==================== INVENTORY EXCEPTIONS ====================

  @ExceptionHandler({InventoryException.class, InsufficientStockException.class})
  public ResponseEntity<ErrorResponse> handleInventoryException(
      BaseECommerceException ex, HttpServletRequest request) {
    ErrorResponse errorResponse = ErrorResponse.detailed(
        HttpStatus.BAD_REQUEST.value(),
        ex.getErrorCode(),
        ex.getMessage(),
        request.getRequestURI(),
        request.getMethod()
    );
    logException(ex, request);
    return buildErrorResponse(errorResponse, HttpStatus.BAD_REQUEST);
  }

  // ==================== PAYMENT EXCEPTIONS ====================

  @ExceptionHandler({PaymentException.class, PaymentMethodNotSupportedException.class,
      PaymentLimitExceededException.class, PaymentProcessingException.class,
      PaymentAuthorizationFailedException.class, PaymentCaptureFailedException.class})
  public ResponseEntity<ErrorResponse> handlePaymentException(
      BaseECommerceException ex, HttpServletRequest request) {
    ErrorResponse errorResponse = ErrorResponse.detailed(
        HttpStatus.BAD_REQUEST.value(),
        ex.getErrorCode(),
        ex.getMessage(),
        request.getRequestURI(),
        request.getMethod()
    );
    logException(ex, request);
    return buildErrorResponse(errorResponse, HttpStatus.BAD_REQUEST);
  }

  // ==================== CUSTOMER EXCEPTIONS ====================

  @ExceptionHandler({CustomerException.class, PurchaseLimitExceededException.class})
  public ResponseEntity<ErrorResponse> handleCustomerException(
      BaseECommerceException ex, HttpServletRequest request) {
    ErrorResponse errorResponse = ErrorResponse.detailed(
        HttpStatus.BAD_REQUEST.value(),
        ex.getErrorCode(),
        ex.getMessage(),
        request.getRequestURI(),
        request.getMethod()
    );
    logException(ex, request);
    return buildErrorResponse(errorResponse, HttpStatus.BAD_REQUEST);
  }

  // ==================== SHIPPING EXCEPTIONS ====================

  @ExceptionHandler({ShippingException.class, ShippingMethodNotAvailableException.class,
      InvalidShippingAddressException.class})
  public ResponseEntity<ErrorResponse> handleShippingException(
      BaseECommerceException ex, HttpServletRequest request) {
    ErrorResponse errorResponse = ErrorResponse.detailed(
        HttpStatus.BAD_REQUEST.value(),
        ex.getErrorCode(),
        ex.getMessage(),
        request.getRequestURI(),
        request.getMethod()
    );
    return buildErrorResponse(errorResponse, HttpStatus.BAD_REQUEST);
  }

  // ==================== DISCOUNT/COUPON EXCEPTIONS ====================

  @ExceptionHandler({DiscountException.class, InvalidCouponException.class,
      CouponExpiredException.class, CouponUsageLimitExceededException.class})
  public ResponseEntity<ErrorResponse> handleDiscountException(
      BaseECommerceException ex, HttpServletRequest request) {
    ErrorResponse errorResponse = ErrorResponse.detailed(
        HttpStatus.BAD_REQUEST.value(),
        ex.getErrorCode(),
        ex.getMessage(),
        request.getRequestURI(),
        request.getMethod()
    );
    return buildErrorResponse(errorResponse, HttpStatus.BAD_REQUEST);
  }

  // ==================== TRANSACTION EXCEPTIONS ====================

  @ExceptionHandler({TransactionException.class, RefundProcessingException.class})
  public ResponseEntity<ErrorResponse> handleTransactionException(
      BaseECommerceException ex, HttpServletRequest request) {
    ErrorResponse errorResponse = ErrorResponse.detailed(
        HttpStatus.BAD_REQUEST.value(),
        ex.getErrorCode(),
        ex.getMessage(),
        request.getRequestURI(),
        request.getMethod()
    );
    logException(ex, request);
    return buildErrorResponse(errorResponse, HttpStatus.BAD_REQUEST);
  }

  // ==================== COMPLIANCE EXCEPTIONS ====================

  @ExceptionHandler({ComplianceException.class, FraudDetectionException.class,
      AgeVerificationException.class})
  public ResponseEntity<ErrorResponse> handleComplianceException(
      BaseECommerceException ex, HttpServletRequest request) {
    ErrorResponse errorResponse = ErrorResponse.detailed(
        HttpStatus.FORBIDDEN.value(),
        ex.getErrorCode(),
        ex.getMessage(),
        request.getRequestURI(),
        request.getMethod()
    );
    logException(ex, request);
    return buildErrorResponse(errorResponse, HttpStatus.FORBIDDEN);
  }

  // ==================== SERVICE EXCEPTIONS ====================

  @ExceptionHandler({ServiceUnavailableException.class, SystemMaintenanceException.class})
  public ResponseEntity<ErrorResponse> handleServiceUnavailableException(
      BaseECommerceException ex, HttpServletRequest request) {
    ErrorResponse errorResponse = ErrorResponse.detailed(
        HttpStatus.SERVICE_UNAVAILABLE.value(),
        ex.getErrorCode(),
        ex.getMessage(),
        request.getRequestURI(),
        request.getMethod()
    );
    logException(ex, request);
    return buildErrorResponse(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
  }

  @ExceptionHandler({ThirdPartyServiceException.class, PaymentGatewayException.class,
      ShippingServiceException.class, NotificationServiceException.class})
  public ResponseEntity<ErrorResponse> handleThirdPartyServiceException(
      BaseECommerceException ex, HttpServletRequest request) {
    ErrorResponse errorResponse = ErrorResponse.detailed(
        HttpStatus.BAD_GATEWAY.value(),
        ex.getErrorCode(),
        ex.getMessage(),
        request.getRequestURI(),
        request.getMethod()
    );
    logException(ex, request);
    return buildErrorResponse(errorResponse, HttpStatus.BAD_GATEWAY);
  }

  // ==================== RESOURCE EXCEPTIONS ====================

  @ExceptionHandler({ResourceNotFoundException.class, EntityNotFoundException.class,
      NoResourceFoundException.class})
  public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
      Exception ex, HttpServletRequest request) {
    String errorCode = ex instanceof BaseECommerceException ?
        ((BaseECommerceException) ex).getErrorCode() : "RESOURCE_NOT_FOUND";
    String message =
        ex.getMessage() != null ? ex.getMessage() : "The requested resource was not found.";
    ErrorResponse errorResponse = ErrorResponse.detailed(
        HttpStatus.NOT_FOUND.value(),
        errorCode,
        message,
        request.getRequestURI(),
        request.getMethod()
    );
    return buildErrorResponse(errorResponse, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler({DuplicateResourceException.class, DuplicateTransactionException.class,
      IllegalStateException.class})
  public ResponseEntity<ErrorResponse> handleConflictException(
      Exception ex, HttpServletRequest request) {
    String errorCode = ex instanceof BaseECommerceException ?
        ((BaseECommerceException) ex).getErrorCode() : "CONFLICT";
    ErrorResponse errorResponse = ErrorResponse.detailed(
        HttpStatus.CONFLICT.value(),
        errorCode,
        ex.getMessage(),
        request.getRequestURI(),
        request.getMethod()
    );
    return buildErrorResponse(errorResponse, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
      DataIntegrityViolationException ex, HttpServletRequest request) {
    log.error("Data integrity violation: {}", ex.getMessage(), ex);
    String message = "Data integrity violation. The operation conflicts with existing data.";
    
    // Try to extract more specific error message
    String rootCause = ex.getRootCause() != null ? ex.getRootCause().getMessage() : null;
    if (rootCause != null) {
      // Check for common constraint violations
      if (rootCause.contains("UNIQUE") || rootCause.contains("unique constraint") || 
          rootCause.contains("Duplicate entry")) {
        message = "A record with this value already exists. Please use a different value.";
      } else if (rootCause.contains("FOREIGN KEY") || rootCause.contains("foreign key constraint")) {
        message = "Cannot perform this operation due to related data constraints.";
      } else if (rootCause.contains("NOT NULL") || rootCause.contains("null constraint")) {
        message = "Required fields cannot be null.";
      }
    }
    
    ErrorResponse errorResponse = ErrorResponse.detailed(
        HttpStatus.CONFLICT.value(),
        "DATA_INTEGRITY_VIOLATION",
        message,
        request.getRequestURI(),
        request.getMethod()
    );
    return buildErrorResponse(errorResponse, HttpStatus.CONFLICT);
  }

  @ExceptionHandler({CustomerBlockedException.class, TransactionExpiredException.class})
  public ResponseEntity<ErrorResponse> handleExpiredOrBlockedException(
      BaseECommerceException ex, HttpServletRequest request) {
    ErrorResponse errorResponse = ErrorResponse.detailed(
        HttpStatus.GONE.value(),
        ex.getErrorCode(),
        ex.getMessage(),
        request.getRequestURI(),
        request.getMethod()
    );
    return buildErrorResponse(errorResponse, HttpStatus.GONE);
  }

  // ==================== SECURITY EXCEPTIONS ====================

  @ExceptionHandler({UnauthorizedException.class})
  public ResponseEntity<ErrorResponse> handleUnauthorizedException(
      UnauthorizedException ex, HttpServletRequest request) {
    ErrorResponse errorResponse = ErrorResponse.detailed(
        HttpStatus.UNAUTHORIZED.value(),
        ex.getErrorCode(),
        ex.getMessage(),
        request.getRequestURI(),
        request.getMethod()
    );
    return buildErrorResponse(errorResponse, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler({AuthorizationException.class, ForbiddenException.class,
      AccessDeniedException.class})
  public ResponseEntity<ErrorResponse> handleAuthorizationException(
      Exception ex, HttpServletRequest request) {
    String errorCode = ex instanceof BaseECommerceException ?
        ((BaseECommerceException) ex).getErrorCode() : "FORBIDDEN";
    String message = ex.getMessage() != null ? ex.getMessage() :
        "You do not have permission to perform this action";
    ErrorResponse errorResponse = ErrorResponse.detailed(
        HttpStatus.FORBIDDEN.value(),
        errorCode,
        message,
        request.getRequestURI(),
        request.getMethod()
    );
    logAuthorizationFailure(ex, request);
    return buildErrorResponse(errorResponse, HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler({AccountLockedException.class, LockedException.class})
  public ResponseEntity<ErrorResponse> handleAccountLockedException(
      Exception ex, HttpServletRequest request) {
    String errorCode = ex instanceof BaseECommerceException ?
        ((BaseECommerceException) ex).getErrorCode() : "ACCOUNT_LOCKED";
    String message = ex instanceof BaseECommerceException && ex.getMessage() != null ?
        ex.getMessage() : (ex.getMessage() != null ? ex.getMessage() :
        "Your account has been locked for security reasons. Please try again later or contact support.");
    ErrorResponse errorResponse = ErrorResponse.detailed(
        HttpStatus.LOCKED.value(),
        errorCode,
        message,
        request.getRequestURI(),
        request.getMethod()
    );
    String clientIp = getClientIpAddress(request);
    String userAgent = request.getHeader("User-Agent");
    log.warn(
        "[SECURITY] Account locked - Method: {} | URI: {} | IP: {} | User-Agent: {} | Message: {}",
        request.getMethod(), request.getRequestURI(), clientIp, userAgent, message);
    return buildErrorResponse(errorResponse, HttpStatus.LOCKED);
  }

  @ExceptionHandler({AccountExpiredException.class, AccountSuspendedException.class,
      CredentialsExpiredException.class})
  public ResponseEntity<ErrorResponse> handleAccountStateException(
      BaseECommerceException ex, HttpServletRequest request) {
    HttpStatus status = ex instanceof AccountExpiredException ? HttpStatus.GONE :
        ex instanceof CredentialsExpiredException ? HttpStatus.UNAUTHORIZED :
            HttpStatus.FORBIDDEN;
    ErrorResponse errorResponse = ErrorResponse.detailed(
        status.value(),
        ex.getErrorCode(),
        ex.getMessage(),
        request.getRequestURI(),
        request.getMethod()
    );
    String clientIp = getClientIpAddress(request);
    String userAgent = request.getHeader("User-Agent");
    log.warn(
        "[SECURITY] Account state exception - Method: {} | URI: {} | IP: {} | User-Agent: {} | Message: {}",
        request.getMethod(), request.getRequestURI(), clientIp, userAgent, ex.getMessage());
    return buildErrorResponse(errorResponse, status);
  }

  @ExceptionHandler(LoginAttemptsException.class)
  public ResponseEntity<ErrorResponse> handleLoginAttemptsException(
      LoginAttemptsException ex, HttpServletRequest request) {
    ErrorResponse errorResponse = ErrorResponse.detailed(
        HttpStatus.TOO_MANY_REQUESTS.value(),
        ex.getErrorCode(),
        ex.getMessage(),
        request.getRequestURI(),
        request.getMethod()
    );
    return buildErrorResponse(errorResponse, HttpStatus.TOO_MANY_REQUESTS);
  }

  @ExceptionHandler(UsernameNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(
      UsernameNotFoundException ex, HttpServletRequest request) {
    String clientIp = getClientIpAddress(request);
    String userAgent = request.getHeader("User-Agent");
    log.warn(
        "[SECURITY] UsernameNotFoundException - Method: {} | URI: {} | IP: {} | User-Agent: {} | Message: {}",
        request.getMethod(), request.getRequestURI(), clientIp, userAgent, ex.getMessage());
    String requestPath = request.getRequestURI();
    boolean isLoginAttempt = requestPath != null &&
        (requestPath.contains("/auth/login") || requestPath.contains("/auth/authenticate"));
    String errorCode = isLoginAttempt ? "AUTHENTICATION_FAILED" : "INVALID_TOKEN";
    String message = isLoginAttempt ?
        "Invalid credentials. Please check your login information and try again." :
        "Your token is no longer valid. Please log in again.";
    ErrorResponse errorResponse = ErrorResponse.detailed(
        HttpStatus.UNAUTHORIZED.value(),
        errorCode,
        message,
        request.getRequestURI(),
        request.getMethod()
    );
    return buildErrorResponse(errorResponse, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ErrorResponse> handleAuthenticationException(
      AuthenticationException ex, HttpServletRequest request) {
    String errorCode =
        ex instanceof BadCredentialsException ? "INVALID_CREDENTIALS" : "AUTHENTICATION_FAILED";
    String message = ex instanceof BadCredentialsException ?
        "Invalid credentials. Please check your login information and try again." :
        (ex.getMessage() != null ? ex.getMessage()
            : "Authentication failed. Please check your credentials and try again.");
    ErrorResponse errorResponse = ErrorResponse.detailed(
        HttpStatus.UNAUTHORIZED.value(),
        errorCode,
        message,
        request.getRequestURI(),
        request.getMethod()
    );
    String clientIp = getClientIpAddress(request);
    String userAgent = request.getHeader("User-Agent");
    log.warn(
        "[SECURITY] Authentication exception - Method: {} | URI: {} | IP: {} | User-Agent: {} | Message: {}",
        request.getMethod(), request.getRequestURI(), clientIp, userAgent, ex.getMessage());
    return buildErrorResponse(errorResponse, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(DisabledException.class)
  public ResponseEntity<ErrorResponse> handleDisabledException(
      DisabledException ex, HttpServletRequest request) {
    String message = ex.getMessage() != null ? ex.getMessage() :
        "Your account cannot be accessed at this time. Please contact support for assistance.";
    ErrorResponse errorResponse = ErrorResponse.detailed(
        HttpStatus.FORBIDDEN.value(),
        "ACCOUNT_DISABLED",
        message,
        request.getRequestURI(),
        request.getMethod()
    );
    String clientIp = getClientIpAddress(request);
    String userAgent = request.getHeader("User-Agent");
    log.warn(
        "[SECURITY] Account disabled - Method: {} | URI: {} | IP: {} | User-Agent: {} | Message: {}",
        request.getMethod(), request.getRequestURI(), clientIp, userAgent, message);
    return buildErrorResponse(errorResponse, HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(InternalAuthenticationServiceException.class)
  public ResponseEntity<ErrorResponse> handleInternalAuthenticationServiceException(
      InternalAuthenticationServiceException ex, HttpServletRequest request) {
    Throwable rootCause = ex.getCause();
    if (rootCause instanceof DisabledException) {
      return handleDisabledException((DisabledException) rootCause, request);
    }
    if (rootCause instanceof AccountExpiredException) {
      return handleAccountStateException((AccountExpiredException) rootCause, request);
    }
    if (rootCause instanceof AccountLockedException) {
      return handleAccountLockedException((AccountLockedException) rootCause, request);
    }
    if (rootCause instanceof AccountSuspendedException) {
      return handleAccountStateException((AccountSuspendedException) rootCause, request);
    }
    if (rootCause instanceof CredentialsExpiredException) {
      return handleAccountStateException((CredentialsExpiredException) rootCause, request);
    }
    if (rootCause instanceof LockedException) {
      return handleAccountLockedException((LockedException) rootCause, request);
    }
    String message = rootCause != null && rootCause.getMessage() != null ?
        rootCause.getMessage() : (ex.getMessage() != null ? ex.getMessage() :
        "Authentication service error occurred. Please try again or contact support.");
    String errorCode = rootCause instanceof BaseECommerceException ?
        ((BaseECommerceException) rootCause).getErrorCode() : "AUTHENTICATION_SERVICE_ERROR";
    ErrorResponse errorResponse = ErrorResponse.detailed(
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        errorCode,
        message,
        request.getRequestURI(),
        request.getMethod()
    );
    String clientIp = getClientIpAddress(request);
    String userAgent = request.getHeader("User-Agent");
    log.error(
        "[SECURITY] Internal authentication service error - Method: {} | URI: {} | IP: {} | User-Agent: {} | Root Cause: {} | Message: {}",
        request.getMethod(), request.getRequestURI(), clientIp, userAgent,
        rootCause != null ? rootCause.getClass().getSimpleName() : "Unknown", message, ex);
    return buildErrorResponse(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  // ==================== GENERAL HTTP EXCEPTIONS ====================

  @ExceptionHandler({BadRequestException.class, MultipartException.class,
      HttpMessageNotReadableException.class, HttpRequestMethodNotSupportedException.class,
      MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class,
      IllegalArgumentException.class, ConstraintViolationException.class})
  public ResponseEntity<ErrorResponse> handleBadRequestException(
      Exception ex, HttpServletRequest request) {
    String errorCode = ex instanceof BaseECommerceException ?
        ((BaseECommerceException) ex).getErrorCode() : "BAD_REQUEST";
    ErrorResponse errorResponse = ErrorResponse.detailed(
        HttpStatus.BAD_REQUEST.value(),
        errorCode,
        ex.getMessage(),
        request.getRequestURI(),
        request.getMethod()
    );
    return buildErrorResponse(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(MissingRequestHeaderException.class)
  public ResponseEntity<ErrorResponse> handleMissingRequestHeader(
      MissingRequestHeaderException ex, HttpServletRequest request) {
    String message = "Missing required header: " + ex.getHeaderName();
    ErrorResponse errorResponse = ErrorResponse.detailed(
        HttpStatus.BAD_REQUEST.value(),
        "MISSING_HEADER",
        message,
        request.getRequestURI(),
        request.getMethod()
    );
    return buildErrorResponse(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ErrorResponse> handleResponseStatusException(
      ResponseStatusException ex, HttpServletRequest request) {
    ErrorResponse errorResponse = ErrorResponse.detailed(
        ex.getStatusCode().value(),
        "RESPONSE_STATUS_ERROR",
        ex.getReason() != null ? ex.getReason() : ex.getMessage(),
        request.getRequestURI(),
        request.getMethod()
    );
    return buildErrorResponse(errorResponse, HttpStatus.valueOf(ex.getStatusCode().value()));
  }

  // ==================== PAYMENT GATEWAY / NETWORK / TRANSACTION TIMEOUT ====================

  @ExceptionHandler(SoapCommunicationException.class)
  public ResponseEntity<ErrorResponse> handleSoapCommunicationException(
      SoapCommunicationException ex, HttpServletRequest request) {
    log.warn("Payment gateway SOAP communication failed: {} | URI: {}",
        ex.getMessage(), request.getRequestURI(), ex);
    ErrorResponse errorResponse = ErrorResponse.detailed(
        HttpStatus.BAD_GATEWAY.value(),
        ex.getErrorCode(),
        "Payment provider is temporarily unavailable (connection or timeout). Please try again later.",
        request.getRequestURI(),
        request.getMethod()
    );
    return buildErrorResponse(errorResponse, HttpStatus.BAD_GATEWAY);
  }

  @ExceptionHandler(WebServiceIOException.class)
  public ResponseEntity<ErrorResponse> handleWebServiceIOException(
      WebServiceIOException ex, HttpServletRequest request) {
    log.warn("Payment gateway I/O error: {} | URI: {}", ex.getMessage(), request.getRequestURI(), ex);
    String message = "Payment provider did not respond in time or connection failed. Please try again later.";
    ErrorResponse errorResponse = ErrorResponse.detailed(
        HttpStatus.GATEWAY_TIMEOUT.value(),
        "PAYMENT_GATEWAY_IO_ERROR",
        message,
        request.getRequestURI(),
        request.getMethod()
    );
    return buildErrorResponse(errorResponse, HttpStatus.GATEWAY_TIMEOUT);
  }

  @ExceptionHandler(SocketTimeoutException.class)
  public ResponseEntity<ErrorResponse> handleSocketTimeoutException(
      SocketTimeoutException ex, HttpServletRequest request) {
    log.warn("Socket timeout: {} | URI: {}", ex.getMessage(), request.getRequestURI(), ex);
    ErrorResponse errorResponse = ErrorResponse.detailed(
        HttpStatus.GATEWAY_TIMEOUT.value(),
        "CONNECTION_TIMEOUT",
        "The request timed out while connecting to an external service. Please try again.",
        request.getRequestURI(),
        request.getMethod()
    );
    return buildErrorResponse(errorResponse, HttpStatus.GATEWAY_TIMEOUT);
  }

  @ExceptionHandler(JpaSystemException.class)
  public ResponseEntity<ErrorResponse> handleJpaSystemException(
      JpaSystemException ex, HttpServletRequest request) {
    boolean isTransactionTimeout = isTransactionTimeout(ex);
    if (isTransactionTimeout) {
      log.warn("Transaction timeout: {} | URI: {}", ex.getMessage(), request.getRequestURI(), ex);
      ErrorResponse errorResponse = ErrorResponse.detailed(
          HttpStatus.GATEWAY_TIMEOUT.value(),
          "TRANSACTION_TIMEOUT",
          "The request took too long to complete. Please try again.",
          request.getRequestURI(),
          request.getMethod()
      );
      return buildErrorResponse(errorResponse, HttpStatus.GATEWAY_TIMEOUT);
    }
    log.error("JPA system error: {} | URI: {}", ex.getMessage(), request.getRequestURI(), ex);
    ErrorResponse errorResponse = ErrorResponse.detailed(
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        "PERSISTENCE_ERROR",
        "A database error occurred. Please try again or contact support.",
        request.getRequestURI(),
        request.getMethod()
    );
    return buildErrorResponse(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  // ==================== GLOBAL EXCEPTION HANDLER ====================

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(
      Exception ex, HttpServletRequest request) {
    log.error("INTERNAL_SERVER_ERROR: {}", ex.getMessage(), ex);
    ErrorResponse errorResponse = ErrorResponse.detailed(
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        "INTERNAL_SERVER_ERROR",
        "Internal system error. Please try again or contact support.",
        request.getRequestURI(),
        request.getMethod()
    );
    return buildErrorResponse(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  // ==================== UTILITY METHODS ====================

  private ResponseEntity<ErrorResponse> buildErrorResponse(ErrorResponse errorResponse,
      HttpStatus httpStatus) {
    ErrorResponse responseWithTrace = attachTraceContext(errorResponse);
    return ResponseEntity.status(httpStatus).body(responseWithTrace);
  }

  private ErrorResponse attachTraceContext(ErrorResponse errorResponse) {
    String traceId = resolveTraceId();
    if (errorResponse.getTraceId() != null && !errorResponse.getTraceId().isBlank()) {
      return errorResponse;
    }
    if (traceId == null || traceId.isBlank()) {
      return errorResponse;
    }
    return errorResponse.toBuilder().traceId(traceId).build();
  }

  private String resolveTraceId() {
    String traceId = MDC.get("traceId");
    RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
    if (requestAttributes instanceof ServletRequestAttributes servletAttributes) {
      HttpServletRequest currentRequest = servletAttributes.getRequest();
      Object traceIdAttribute = currentRequest.getAttribute("TRACE_ID");
      if (traceIdAttribute instanceof String attributeValue && !attributeValue.isBlank()) {
        traceId = attributeValue;
      }
    }
    return traceId;
  }

  private Map<String, String> processFieldErrors(BindingResult bindingResult) {
    Map<String, String> errorMap = new HashMap<>();
    for (FieldError error : bindingResult.getFieldErrors()) {
      errorMap.put(error.getField(), error.getDefaultMessage());
    }
    return errorMap;
  }

  private HttpStatus mapExceptionToHttpStatus(BaseECommerceException ex) {
    String errorCode = ex.getErrorCode();
    return switch (errorCode) {
      case "BAD_REQUEST", "VALIDATION_ERROR", "BINDING_ERROR", "PRODUCT_ERROR",
           "ORDER_ERROR", "CART_ERROR", "INVENTORY_ERROR", "PAYMENT_ERROR",
           "CUSTOMER_ERROR", "SHIPPING_ERROR", "DISCOUNT_ERROR", "TRANSACTION_ERROR",
           "ORDER_PROCESSING_ERROR", "PAYMENT_PROCESSING_ERROR", "REFUND_PROCESSING_ERROR",
           "PRODUCT_OUT_OF_STOCK", "INSUFFICIENT_STOCK", "CART_LIMIT_EXCEEDED",
           "PAYMENT_METHOD_NOT_SUPPORTED", "PAYMENT_LIMIT_EXCEEDED", "PURCHASE_LIMIT_EXCEEDED",
           "SHIPPING_METHOD_NOT_AVAILABLE", "INVALID_SHIPPING_ADDRESS", "INVALID_COUPON",
           "COUPON_EXPIRED", "COUPON_USAGE_LIMIT_EXCEEDED", "ORDER_CANCELLATION_FAILED" ->
          HttpStatus.BAD_REQUEST;
      case "UNAUTHORIZED", "AUTHENTICATION_FAILED", "INVALID_CREDENTIALS", "INVALID_TOKEN",
           "CREDENTIALS_EXPIRED" -> HttpStatus.UNAUTHORIZED;
      case "AUTHORIZATION_FAILED", "FORBIDDEN", "COMPLIANCE_VIOLATION", "FRAUD_DETECTED",
           "AGE_VERIFICATION_FAILED", "ACCOUNT_SUSPENDED", "ACCOUNT_DISABLED" ->
          HttpStatus.FORBIDDEN;
      case "RESOURCE_NOT_FOUND" -> HttpStatus.NOT_FOUND;
      case "DUPLICATE_RESOURCE", "DUPLICATE_TRANSACTION" -> HttpStatus.CONFLICT;
      case "TOO_MANY_LOGIN_ATTEMPTS" -> HttpStatus.TOO_MANY_REQUESTS;
      case "TRANSACTION_EXPIRED", "CUSTOMER_BLOCKED", "ACCOUNT_EXPIRED" -> HttpStatus.GONE;
      case "ACCOUNT_LOCKED" -> HttpStatus.LOCKED;
      case "SERVICE_UNAVAILABLE", "SYSTEM_MAINTENANCE" -> HttpStatus.SERVICE_UNAVAILABLE;
      case "THIRD_PARTY_SERVICE_ERROR", "PAYMENT_GATEWAY_ERROR", "SHIPPING_SERVICE_ERROR",
           "NOTIFICATION_SERVICE_ERROR" -> HttpStatus.BAD_GATEWAY;
      default -> HttpStatus.INTERNAL_SERVER_ERROR;
    };
  }

  private void logException(BaseECommerceException ex, HttpServletRequest request) {
    log.warn("Exception [{}] occurred at [{} {}]: {}", ex.getErrorCode(), request.getMethod(),
        request.getRequestURI(), ex.getMessage(), ex);
  }

  private void logAuthorizationFailure(Exception ex, HttpServletRequest request) {
    String clientIp = getClientIpAddress(request);
    String userAgent = request.getHeader("User-Agent");
    log.warn(
        "[SECURITY] Authorization failure - Method: {} | URI: {} | IP: {} | User-Agent: {} | Message: {}",
        request.getMethod(), request.getRequestURI(), clientIp, userAgent, ex.getMessage());
  }

  private boolean isTransactionTimeout(JpaSystemException ex) {
    String message = ex.getMessage();
    if (message != null && message.toLowerCase().contains("transaction timeout")) {
      return true;
    }
    Throwable cause = ex.getCause();
    while (cause != null) {
      if (cause.getMessage() != null
          && cause.getMessage().toLowerCase().contains("transaction timeout")) {
        return true;
      }
      cause = cause.getCause();
    }
    return false;
  }

  private String getClientIpAddress(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(
        xForwardedFor)) {
      return xForwardedFor.split(",")[0].trim();
    }
    String xRealIp = request.getHeader("X-Real-IP");
    if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
      return xRealIp;
    }
    return request.getRemoteAddr();
  }
}

