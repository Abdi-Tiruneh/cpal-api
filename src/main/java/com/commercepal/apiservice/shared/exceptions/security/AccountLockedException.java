package com.commercepal.apiservice.shared.exceptions.security;

import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;

/**
 * Exception thrown when an account has been locked and access is temporarily restricted.
 *
 * <p>This exception is thrown during authentication when a user's account has been locked,
 * typically due to security reasons such as:
 * <ul>
 *   <li>Multiple failed login attempts</li>
 *   <li>Suspicious activity detection</li>
 *   <li>Administrative action</li>
 *   <li>Policy violations</li>
 * </ul>
 *
 * <p>When this exception is thrown, the user should be informed that their account is locked
 * and may need to wait for an automatic unlock period, contact support, or follow specific
 * unlock procedures.
 *
 * @see BaseECommerceException
 * @since 1.0
 */
public class AccountLockedException extends BaseECommerceException {

  private static final String ERROR_CODE = "ACCOUNT_LOCKED";
  private static final String DEFAULT_MESSAGE = "Your account has been locked for security reasons. Please try again later or contact support.";

  /**
   * Constructs a new AccountLockedException with the default error code and message.
   */
  public AccountLockedException() {
    super(ERROR_CODE, DEFAULT_MESSAGE);
  }

  /**
   * Constructs a new AccountLockedException with the default error code and a custom message.
   *
   * @param customMessage the custom error message providing specific details about why the account
   *                      was locked
   */
  public AccountLockedException(String customMessage) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage);
  }

  /**
   * Constructs a new AccountLockedException with the default error code, custom message, and
   * cause.
   *
   * @param customMessage the custom error message providing specific details about why the account
   *                      was locked
   * @param cause         the cause of this exception (which is saved for later retrieval by the
   *                      getCause() method)
   */
  public AccountLockedException(String customMessage, Throwable cause) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage, cause);
  }
}

