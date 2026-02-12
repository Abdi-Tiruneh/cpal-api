package com.commercepal.apiservice.payments.integration.pesapal;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "payment.pesapal")
@Data
public class PesapalConfig {
    private String baseUrl;
    private String consumerKey;
    private String consumerSecret;
    private String ipn;
    private String ipnUrl;
    private String callbackUrl;
}
