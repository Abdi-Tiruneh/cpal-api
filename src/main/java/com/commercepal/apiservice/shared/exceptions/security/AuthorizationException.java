package com.commercepal.apiservice.shared.exceptions.security;

import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a user is not authorized to perform a specific operation or access a
 * resource. This exception is used for permission/authorization failures (HTTP 403 Forbidden).
 *
 * <p>
 * Use this exception when:
 * <ul>
 * <li>User is authenticated but lacks required permissions</li>
 * <li>User type does not have access to the resource</li>
 * <li>User role does not allow the operation</li>
 * <li>User is trying to manage resources they don't own</li>
 * </ul>
 * </p>
 *
 * @see UnauthorizedException for authentication failures (HTTP 401)
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class AuthorizationException extends BaseECommerceException {

  private static final String ERROR_CODE = "AUTHORIZATION_FAILED";
  private static final String DEFAULT_MESSAGE = "You do not have permission to perform this action";

  public AuthorizationException() {
    super(ERROR_CODE, DEFAULT_MESSAGE);
  }

  public AuthorizationException(String customMessage) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage);
  }

  public AuthorizationException(String customMessage, Throwable cause) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage, cause);
  }

  /**
   * Create an authorization exception for a specific resource type
   *
   * @param resourceType Type of resource being accessed
   * @return AuthorizationException with resource-specific message
   */
  public static AuthorizationException forResource(String resourceType) {
    return new AuthorizationException(
        String.format("You do not have permission to access this %s", resourceType));
  }

  /**
   * Create an authorization exception for a specific action
   *
   * @param action Action being attempted
   * @return AuthorizationException with action-specific message
   */
  public static AuthorizationException forAction(String action) {
    return new AuthorizationException(
        String.format("You do not have permission to %s", action));
  }

  /**
   * Create an authorization exception for user type requirement
   *
   * @param requiredUserType Required user type
   * @return AuthorizationException with user type message
   */
  public static AuthorizationException forUserType(String requiredUserType) {
    return new AuthorizationException(
        String.format("Access denied. %s account required.", requiredUserType));
  }

  /**
   * Create an authorization exception for role requirement
   *
   * @param requiredRole Required role
   * @return AuthorizationException with role message
   */
  public static AuthorizationException forRole(String requiredRole) {
    return new AuthorizationException(
        String.format("Access denied. %s role required.", requiredRole));
  }

  /**
   * Create an authorization exception for permission requirement
   *
   * @param requiredPermission Required permission
   * @return AuthorizationException with permission message
   */
  public static AuthorizationException forPermission(String requiredPermission) {
    return new AuthorizationException(
        String.format("Access denied. %s permission required.", requiredPermission));
  }

  /**
   * Create an authorization exception for resource ownership
   *
   * @return AuthorizationException with ownership message
   */
  public static AuthorizationException notOwner() {
    return new AuthorizationException(
        "Access denied. You can only manage your own resources.");
  }

  /**
   * Create an authorization exception for admin-only operations
   *
   * @return AuthorizationException with admin message
   */
  public static AuthorizationException adminOnly() {
    return new AuthorizationException(
        "Access denied. Administrator privileges required.");
  }
}

