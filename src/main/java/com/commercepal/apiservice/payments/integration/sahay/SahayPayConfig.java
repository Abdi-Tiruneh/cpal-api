package com.commercepal.apiservice.payments.integration.sahay;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "payment.sahaypay")
@Data
public class SahayPayConfig
{
    private String baseUrl;
    private String username;
    private String password;
    private String callbackUrl;
}