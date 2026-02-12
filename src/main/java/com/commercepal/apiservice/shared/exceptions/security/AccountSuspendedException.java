package com.commercepal.apiservice.shared.exceptions.security;

import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;

/**
 * Exception thrown when an account has been suspended and access is restricted.
 *
 * <p>This exception is thrown during authentication when a user's account has been suspended,
 * typically due to:
 * <ul>
 *   <li>Policy violations or terms of service breaches</li>
 *   <li>Administrative action requiring review</li>
 *   <li>Compliance or regulatory requirements</li>
 *   <li>Security concerns requiring investigation</li>
 * </ul>
 *
 * <p>Suspension is typically a more serious action than locking, and may require administrative
 * intervention to resolve. When this exception is thrown, the user should be informed that
 * their account is suspended and should contact support for assistance.
 *
 * @see BaseECommerceException
 * @since 1.0
 */
public class AccountSuspendedException extends BaseECommerceException {

  private static final String ERROR_CODE = "ACCOUNT_SUSPENDED";
  private static final String DEFAULT_MESSAGE = "Your account has been suspended. Please contact support for assistance.";

  /**
   * Constructs a new AccountSuspendedException with the default error code and message.
   */
  public AccountSuspendedException() {
    super(ERROR_CODE, DEFAULT_MESSAGE);
  }

  /**
   * Constructs a new AccountSuspendedException with the default error code and a custom message.
   *
   * @param customMessage the custom error message providing specific details about why the account
   *                      was suspended
   */
  public AccountSuspendedException(String customMessage) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage);
  }

  /**
   * Constructs a new AccountSuspendedException with the default error code, custom message, and
   * cause.
   *
   * @param customMessage the custom error message providing specific details about why the account
   *                      was suspended
   * @param cause         the cause of this exception (which is saved for later retrieval by the
   *                      getCause() method)
   */
  public AccountSuspendedException(String customMessage, Throwable cause) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage, cause);
  }
}

