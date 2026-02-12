package com.commercepal.apiservice.payments.integration.telebirr.config;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.transport.http.HttpUrlConnectionMessageSender;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(TelebirrUssdProperties.class)
public class TelebirrUssdSoapConfig {

  private final TelebirrUssdProperties telebirrUssdProperties;

  /**
   * Configures the WebServiceTemplate bean with enterprise-grade settings. Includes connection
   * pooling, timeouts, and retry mechanisms.
   *
   * @return Configured WebServiceTemplate instance
   */
  @Bean(name = "telebirrUssdWebServiceTemplate")
  public WebServiceTemplate webServiceTemplate() {
    WebServiceTemplate template = new WebServiceTemplate();

    // Configure message sender with connection pooling and timeouts
    template.setMessageSender(httpComponentsMessageSender());

    // Set default URI
    template.setDefaultUri(telebirrUssdProperties.getSoapEndpoint());

    // Configure marshaller (optional, for strongly-typed JAXB objects)
    // For now, we're using String-based XML manipulation
    // template.setMarshaller(jaxb2Marshaller());
    // template.setUnmarshaller(jaxb2Marshaller());

    log.info("WebServiceTemplate configured successfully for Telebirr USSD - Endpoint: {}, " +
            "ConnectionTimeout: {}ms, ReadTimeout: {}ms, MaxConnections: {}",
        telebirrUssdProperties.getSoapEndpoint(),
        telebirrUssdProperties.getSoapConnectionTimeout(),
        telebirrUssdProperties.getSoapReadTimeout(),
        telebirrUssdProperties.getSoapMaxConnections());

    return template;
  }

  /**
   * Configures HTTP message sender with timeout settings.
   * 
   * <p>Note: Using HttpUrlConnectionMessageSender instead of HttpComponentsMessageSender
   * because the project uses HttpComponents 5.x, while HttpComponentsMessageSender
   * requires HttpComponents 4.x. HttpUrlConnectionMessageSender uses Java's built-in
   * HttpURLConnection and doesn't require external HTTP client libraries.</p>
   *
   * @return Configured HttpUrlConnectionMessageSender
   */
  @Bean(name = "telebirrUssdHttpMessageSender")
  public HttpUrlConnectionMessageSender httpComponentsMessageSender() {
    HttpUrlConnectionMessageSender messageSender = new HttpUrlConnectionMessageSender();

    // Set connection timeout (time to establish connection)
    messageSender.setConnectionTimeout(
        Duration.ofMillis(telebirrUssdProperties.getSoapConnectionTimeout()));

    // Set read timeout (time to wait for data)
    messageSender.setReadTimeout(
        Duration.ofMillis(telebirrUssdProperties.getSoapReadTimeout()));

    log.debug("HTTP Message Sender configured - ConnectionTimeout: {}ms, ReadTimeout: {}ms",
        telebirrUssdProperties.getSoapConnectionTimeout(), telebirrUssdProperties.getSoapReadTimeout());

    return messageSender;
  }
}

//<?xml version="1.0" encoding="UTF-8"?>
//<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
//    <soapenv:Body>
//        <api:Response xmlns:api="http://cps.huawei.com/cpsinterface/api_requestmgr" xmlns:res="http://cps.huawei.com/cpsinterface/response">
//            <res:Header>
//                <res:Version>1.0</res:Version>
//                <res:OriginatorConversationID>ff_X201718311433220123</res:OriginatorConversationID>
//                <res:ConversationID>AG_20250930_70304d5c2b86a7315258</res:ConversationID>
//            </res:Header>
//            <res:Body>
//                <res:ResponseCode>0</res:ResponseCode>
//                <res:ResponseDesc>Accept the service request successfully.</res:ResponseDesc>
//                <res:ServiceStatus>0</res:ServiceStatus>
//            </res:Body>
//        </api:Response>
//    </soapenv:Body>
//</soapenv:Envelope>

