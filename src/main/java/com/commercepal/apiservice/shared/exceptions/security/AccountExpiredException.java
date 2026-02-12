package com.commercepal.apiservice.shared.exceptions.security;

import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;

/**
 * Exception thrown when an account has expired and is no longer valid for use.
 *
 * <p>This exception is typically thrown during authentication when a user's account
 * has passed its expiration date. Accounts may have expiration dates set for various business
 * reasons such as temporary access, trial periods, or security policies.
 *
 * <p>When this exception is thrown, the user should be informed that their account
 * has expired and they may need to contact support or renew their account to regain access.
 *
 * @see BaseECommerceException
 * @since 1.0
 */
public class AccountExpiredException extends BaseECommerceException {

  private static final String ERROR_CODE = "ACCOUNT_EXPIRED";
  private static final String DEFAULT_MESSAGE = "Your account has expired and is no longer accessible. Please contact support to renew your account.";

  /**
   * Constructs a new AccountExpiredException with the default error code and message.
   */
  public AccountExpiredException() {
    super(ERROR_CODE, DEFAULT_MESSAGE);
  }

  /**
   * Constructs a new AccountExpiredException with the default error code and a custom message.
   *
   * @param customMessage the custom error message providing specific details about the account
   *                      expiration
   */
  public AccountExpiredException(String customMessage) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage);
  }

  /**
   * Constructs a new AccountExpiredException with the default error code, custom message, and
   * cause.
   *
   * @param customMessage the custom error message providing specific details about the account
   *                      expiration
   * @param cause         the cause of this exception (which is saved for later retrieval by the
   *                      getCause() method)
   */
  public AccountExpiredException(String customMessage, Throwable cause) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage, cause);
  }
}

