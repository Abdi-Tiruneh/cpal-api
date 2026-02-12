package com.commercepal.apiservice.shared.exceptions.security;

import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;

/**
 * Exception thrown when user credentials (password, PIN, etc.) have expired and must be renewed.
 *
 * <p>This exception is thrown during authentication when a user's password or other credentials
 * have passed their expiration date. This is a security measure to ensure that credentials are
 * regularly updated to maintain account security.
 *
 * <p>When this exception is thrown, the user should be prompted to update their credentials
 * through the appropriate password reset or credential renewal process.
 *
 * @see BaseECommerceException
 * @since 1.0
 */
public class CredentialsExpiredException extends BaseECommerceException {

  private static final String ERROR_CODE = "CREDENTIALS_EXPIRED";
  private static final String DEFAULT_MESSAGE = "Your password has expired. Please update your credentials to continue.";

  /**
   * Constructs a new CredentialsExpiredException with the default error code and message.
   */
  public CredentialsExpiredException() {
    super(ERROR_CODE, DEFAULT_MESSAGE);
  }

  /**
   * Constructs a new CredentialsExpiredException with the default error code and a custom message.
   *
   * @param customMessage the custom error message providing specific details about the credential
   *                      expiration
   */
  public CredentialsExpiredException(String customMessage) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage);
  }

  /**
   * Constructs a new CredentialsExpiredException with the default error code, custom message, and
   * cause.
   *
   * @param customMessage the custom error message providing specific details about the credential
   *                      expiration
   * @param cause         the cause of this exception (which is saved for later retrieval by the
   *                      getCause() method)
   */
  public CredentialsExpiredException(String customMessage, Throwable cause) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage, cause);
  }
}

