package com.commercepal.apiservice.payments.integration.cbebirr;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "payment.cbe.birr")
@Data
public class CbeBirrConfig {

  private final Payment payment = new Payment();
  private final Util util = new Util();

  /**
   * Convenience accessor for payment.url.
   */
  public String getPaymentUrl() {
    return payment.getUrl();
  }

  /**
   * Convenience accessor for util.service.
   */
  public String getUtilService() {
    return util.getService();
  }

  @Data
  public static class Payment {

    private String url;
  }

  @Data
  public static class Util {

    private String service;
  }
}
