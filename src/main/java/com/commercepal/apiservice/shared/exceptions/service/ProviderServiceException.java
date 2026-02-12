package com.commercepal.apiservice.shared.exceptions.service;

import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;

/**
 * Exception thrown when product provider service operations fail.
 * <p>
 * This exception is used for failures in communication with external product providers such as
 * Amazon, AliExpress, SHEIN, etc. Common scenarios include:
 * <ul>
 *   <li>Network communication failures</li>
 *   <li>Provider service unavailability</li>
 *   <li>Invalid response from provider</li>
 *   <li>Provider API errors</li>
 * </ul>
 */
public class ProviderServiceException extends BaseECommerceException {

  private static final String ERROR_CODE = "PROVIDER_SERVICE_ERROR";
  private static final String DEFAULT_MESSAGE = "Product provider service operation failed";

  /**
   * Creates a new ProviderServiceException with the default message.
   */
  public ProviderServiceException() {
    super(ERROR_CODE, DEFAULT_MESSAGE);
  }

  /**
   * Creates a new ProviderServiceException with a custom message.
   *
   * @param customMessage the custom error message
   */
  public ProviderServiceException(String customMessage) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage);
  }

  /**
   * Creates a new ProviderServiceException with a custom message and cause.
   *
   * @param customMessage the custom error message
   * @param cause         the underlying exception that caused this error
   */
  public ProviderServiceException(String customMessage, Throwable cause) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage, cause);
  }

  /**
   * Creates a new ProviderServiceException for a specific provider.
   *
   * @param providerName  the name of the provider (e.g., "Amazon", "AliExpress")
   * @param customMessage the custom error message
   */
  public ProviderServiceException(String providerName, String customMessage) {
    super(ERROR_CODE, DEFAULT_MESSAGE,
        String.format("Provider '%s' service error: %s", providerName, customMessage));
  }

  /**
   * Creates a new ProviderServiceException for a specific provider with cause.
   *
   * @param providerName  the name of the provider (e.g., "Amazon", "AliExpress")
   * @param customMessage the custom error message
   * @param cause         the underlying exception that caused this error
   */
  public ProviderServiceException(String providerName, String customMessage, Throwable cause) {
    super(ERROR_CODE, DEFAULT_MESSAGE,
        String.format("Provider '%s' service error: %s", providerName, customMessage), cause);
  }
}

