package com.commercepal.apiservice.payments.integration.telebirr.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "payment.telebirr.ussd")
@Validated
@Data
public class TelebirrUssdProperties {

  /**
   * SOAP endpoint URL for Huawei payment service
   */
  @NotBlank(message = "SOAP endpoint URL is required")
  private String soapEndpoint;

  /**
   * Command identifier for the transaction type
   */
  @NotBlank(message = "Command ID is required")
  private String commandId;

  /**
   * Third party identifier (merchant ID)
   */
  @NotBlank(message = "Third party ID is required")
  private String thirdPartyId;

  /**
   * Encrypted password for authentication
   */
  @NotBlank(message = "Password is required")
  private String password;

  /**
   * Callback URL for transaction results
   */
  @NotBlank(message = "Result URL is required")
  private String resultUrl;

  /**
   * Initiator identifier for the transaction
   */
  @NotBlank(message = "Initiator identifier is required")
  private String initiatorIdentifier;

  /**
   * Security credential for authentication
   */
  @NotBlank(message = "Security credential is required")
  private String securityCredential;

  /**
   * Short code for the merchant
   */
  @NotBlank(message = "Short code is required")
  private String shortCode;

  /**
   * Receiver party identifier (typically same as short code)
   */
  @NotBlank(message = "Receiver identifier is required")
  private String receiverIdentifier;

  /**
   * Currency code (e.g., ETB)
   */
  @NotBlank(message = "Currency is required")
  private String currency;

  /**
   * Caller type identifier
   */
  @NotNull(message = "Caller type is required")
  private Integer callerType;

  /**
   * Key owner identifier
   */
  @NotNull(message = "Key owner is required")
  private Integer keyOwner;

  /**
   * Primary party identifier type
   */
  @NotNull(message = "Primary identifier type is required")
  private Integer primaryIdentifierType;

  /**
   * Receiver party identifier type
   */
  @NotNull(message = "Receiver identifier type is required")
  private Integer receiverIdentifierType;

  /**
   * Initiator identifier type
   */
  @NotNull(message = "Initiator identifier type is required")
  private Integer initiatorIdentifierType;

  /**
   * SOAP connection timeout in milliseconds
   */
  @NotNull(message = "SOAP connection timeout is required")
  @Min(value = 1000, message = "Connection timeout must be at least 1000ms")
  private Long soapConnectionTimeout;

  /**
   * SOAP read timeout in milliseconds
   */
  @NotNull(message = "SOAP read timeout is required")
  @Min(value = 1000, message = "Read timeout must be at least 1000ms")
  private Long soapReadTimeout;

  /**
   * Maximum number of SOAP connections
   */
  @NotNull(message = "Max connections is required")
  @Min(value = 1, message = "Max connections must be at least 1")
  private Integer soapMaxConnections;
}

