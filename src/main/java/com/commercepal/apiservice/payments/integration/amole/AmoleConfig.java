package com.commercepal.apiservice.payments.integration.amole;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for Amole payment integration.
 */
@Configuration
@ConfigurationProperties(prefix = "org.amole")
@Data
public class AmoleConfig {

  private final Payment payment = new Payment();
  private String signature;
  private String ipAddress;
  private final Authenticate authenticate = new Authenticate();
  private String tin;

  @Data
  public static class Payment {

    private String url;
    private final Action action = new Action();
  }

  @Data
  public static class Action {

    private String authorization;
    private String payment;
  }

  @Data
  public static class Authenticate {

    private String userName;
    private String password;
    private String amoleMerchantID;
  }
}
