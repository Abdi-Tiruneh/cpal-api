package com.commercepal.apiservice.payments.integration.ebirr;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "payment.ebirr")
@Data
public class EbirrConfig {

  private final Initiate initiate = new Initiate();
  private String schemaVersion;
  private String channelName;
  private String serviceName;
  private String merchantUid;
  private String paymentMethod;
  private String apiKey;
  private String apiUserId;

  /**
   * Convenience accessor for initiate.payment URL.
   */
  public String getInitiatePayment() {
    return initiate.getPayment();
  }

  @Data
  public static class Initiate {

    private String payment;
  }
}
