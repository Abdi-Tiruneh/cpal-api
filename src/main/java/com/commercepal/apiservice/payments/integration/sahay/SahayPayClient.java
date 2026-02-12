package com.commercepal.apiservice.payments.integration.sahay;

import com.commercepal.apiservice.orders.enums.PaymentStatus;
import com.commercepal.apiservice.payments.UserAgentProvider;
import com.commercepal.apiservice.payments.core.ProcessSuccessPayment;
import com.commercepal.apiservice.payments.integration.sahay.dto.CustomerLookupResponse;
import com.commercepal.apiservice.payments.oderPayment.OrderPayment;
import com.commercepal.apiservice.payments.oderPayment.OrderPaymentRepository;
import com.commercepal.apiservice.shared.exceptions.resource.ResourceNotFoundException;
import com.commercepal.apiservice.shared.exceptions.service.ServiceUnavailableException;
import com.commercepal.apiservice.shared.exceptions.transaction.DuplicateTransactionException;
import com.commercepal.apiservice.shared.exceptions.transaction.PaymentProcessingException;
import com.commercepal.apiservice.payments.oderPayment.enums.NextAction;
import com.commercepal.apiservice.payments.oderPayment.dto.PaymentInitiationResponse;
import com.commercepal.apiservice.utils.HttpProcessor;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SahayPayClient {

  private final HttpProcessor httpProcessor;
  private final SahayPayConfig config;
  private final OrderPaymentRepository orderPaymentRepository;
  private final ProcessSuccessPayment processSuccessPayment;

//  private volatile String accessToken;
//  private volatile String refreshToken;
//  private volatile Instant tokenExpiry;

  private static final String SERVICE_NAME = "SahayPay";

  // ==================== PUBLIC METHODS ====================

  public CustomerLookupResponse lookupCustomerAccount(String phoneNumber) {
    String requestId = UUID.randomUUID().toString();

    String formattedPhone = validateAndFormatPhoneNumber(phoneNumber);

    JSONObject requestBody = new JSONObject()
        .put("accountNumber", formattedPhone)
        .put("detType", "1");

    String accessToken = authenticate();

    String url = config.getBaseUrl() + "/account/lookup";
    Map<String, String> headers = new java.util.HashMap<>(Map.of(
        "Content-Type", "application/json",
        "User-Agent", UserAgentProvider.get(),
        "Authorization", "Bearer " + accessToken,
        "X-Request-ID", requestId
    ));

    // Add these critical headers for Cloudflare
    headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
    headers.put("Accept-Language", "en-US,en;q=0.5");
    headers.put("Accept-Encoding", "gzip, deflate, br");
    headers.put("Connection", "keep-alive");
    headers.put("Upgrade-Insecure-Requests", "1");
    headers.put("Sec-Fetch-Dest", "document");
    headers.put("Sec-Fetch-Mode", "navigate");
    headers.put("Sec-Fetch-Site", "none");
    headers.put("Sec-Fetch-User", "?1");
    headers.put("Cache-Control", "max-age=0");
    headers.put("Referer", config.getBaseUrl() + "/");
    headers.put("Origin", config.getBaseUrl());


    log.info("Initiating customer lookup | SahayPayClient | lookupCustomerAccount | phone={}, requestId={}, body={}",
        formattedPhone, requestId, requestBody);

    JSONObject apiResponse = httpProcessor.executeStructuredRequest(url, "POST", requestBody.toString(), headers).join();

    log.debug("Raw API response received | SahayPayClient | lookupCustomerAccount | requestId={}, response={}",
        requestId, apiResponse);

    // Validate HTTP status
    String statusCode = apiResponse.optString("StatusCode", "0");
    String responseBodyStr = apiResponse.optString("ResponseBody", "{}");
    JSONObject responseBody = new JSONObject(responseBodyStr);

    String responseCode = responseBody.optString("response", "999");
    String responseDescription = responseBody.optString("responseDescription", "Unknown error");

    if (!"200".equals(statusCode) && !"201".equals(statusCode)) {
      log.error("Account lookup failed | SahayPayClient | lookupCustomerAccount | phone={}, requestId={}, statusCode={}",
          formattedPhone, requestId, statusCode);

      if ("400".equals(statusCode)) {
        throw new ResourceNotFoundException("Customer account lookup failed: " + responseDescription);
      }

      throw new ServiceUnavailableException(SERVICE_NAME, "Service unavailable");
    }

    if ("000".equals(responseCode)) {
      String customerName = responseBody.optString("name", "Unknown Customer");

      log.info("Customer lookup success | SahayPayClient | lookupCustomerAccount | phone={}, name={}, requestId={}",
          formattedPhone, customerName, requestId);

      return new CustomerLookupResponse(customerName.toUpperCase());
    }

    log.warn("Customer lookup failed | SahayPayClient | lookupCustomerAccount | phone={}, requestId={}, code={}, desc={}",
        formattedPhone, requestId, responseCode, responseDescription);

    throw new ResourceNotFoundException("Customer account lookup failed: " + responseDescription);
  }

  public PaymentInitiationResponse initiateUssdPush(OrderPayment payment) {
    // 1. Validate and prepare request
    String formattedPhoneNumber = validateAndFormatPhoneNumber(payment.getAccountNumber());
    payment.setAccountNumber(formattedPhoneNumber);

    JSONObject requestBody = new JSONObject()
        .put("referenceNumber", payment.getReference())
        .put("phoneNumber", payment.getAccountNumber())
        .put("amount", payment.getAmount())
        .put("callbackUrl", config.getCallbackUrl())
        .put("timestamp", Instant.now().toString());

    payment.recordInitRequest(requestBody.toString());
    orderPaymentRepository.save(payment);

    // 2. Execute API call
    String accessToken = authenticate();

    String url = config.getBaseUrl() + "/transactions";
    String requestId = UUID.randomUUID().toString();
    Map<String, String> headers = new java.util.HashMap<>(Map.of(
        "Content-Type", "application/json",
        "User-Agent", UserAgentProvider.get(),
        "Authorization", "Bearer " + accessToken,
        "X-Request-ID", requestId
    ));

    // Add these critical headers for Cloudflare
    headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
    headers.put("Accept-Language", "en-US,en;q=0.5");
    headers.put("Accept-Encoding", "gzip, deflate, br");
    headers.put("Connection", "keep-alive");
    headers.put("Upgrade-Insecure-Requests", "1");
    headers.put("Sec-Fetch-Dest", "document");
    headers.put("Sec-Fetch-Mode", "navigate");
    headers.put("Sec-Fetch-Site", "none");
    headers.put("Sec-Fetch-User", "?1");
    headers.put("Cache-Control", "max-age=0");
    // Add Referer if you have a previous page
    headers.put("Referer", config.getBaseUrl() + "/");
    // If making API calls, sometimes Origin helps
    headers.put("Origin", config.getBaseUrl());

    log.info("Sending USSD push request | SahayPayClient | initiateUssdPush | reference={}, requestId={}",
        payment.getReference(), requestId);
    JSONObject apiResponse = httpProcessor.executeStructuredRequest(url, "POST", requestBody.toString(), headers).join();

    // 3. Process response
    String statusCode = apiResponse.optString("StatusCode");
    if (!"200".equals(statusCode) && !"201".equals(statusCode)) {
      log.error("HTTP error from SahayPay | SahayPayClient | initiateUssdPush | reference={}, status={}, response={}",
          payment.getReference(), statusCode, apiResponse.toString());
      payment.resolve(PaymentStatus.FAILED, "Service communication failed");
      orderPaymentRepository.save(payment);
      throw new ServiceUnavailableException(SERVICE_NAME, "SahayPay service unavailable");
    }

    // Parse and validate business response
    String responseBodyStr = apiResponse.optString("ResponseBody");
    payment.recordInitResponse(responseBodyStr);
    orderPaymentRepository.save(payment);

    JSONObject responseBody = new JSONObject(responseBodyStr);
    String responseCode = responseBody.optString("response");

    if (!"000".equals(responseCode)) {
      String errorMsg = responseBody.optString("responseDescription", "Unknown business error");
      log.error("Business error | SahayPayClient | initiateUssdPush | reference={}, code={}, message={}, response={}",
          payment.getReference(), responseCode, errorMsg, responseBodyStr);
      payment.resolve(PaymentStatus.FAILED, "SAHAYPAY_" + responseCode + ": " + errorMsg);
      orderPaymentRepository.save(payment);
      throw new PaymentProcessingException("SahayPay payment failed: " + errorMsg);
    }

    // Success case
    payment.setStatus(PaymentStatus.PROCESSING);
    payment.recordInitResponse(responseBody.toString());
    orderPaymentRepository.save(payment);

    log.info("USSD push initiated successfully | SahayPayClient | initiateUssdPush | reference={}, orderId={}",
        payment.getReference(), payment.getOrder().getId());

    return PaymentInitiationResponse.builder()
        .success(true)
        .orderNumber(payment.getOrder().getOrderNumber())
        .paymentReference(payment.getReference())
        .paymentUrl(null)
        .paymentInstructions("Please complete the payment on your phone")
        .nextAction(NextAction.OPEN_ADDITIONAL_INPUT)
        .build();
  }

  public void handleSahayPayCallback(JSONObject requestBody) {
    log.info("Incoming callback received | SahayPayClient | handleSahayPayCallback | body={}",
        requestBody.toString(4));

    String paymentReference = requestBody.getString("partnerBillRef");
    String response = requestBody.getString("response");
    String responseDescription = requestBody.getString("responseDescription");
    String gatewayReference = requestBody.optString("referenceNumber");
    BigDecimal callbackAmount = requestBody.optBigDecimal("amount", BigDecimal.ZERO);
    String callbackPhoneNumber = requestBody.optString("phoneNumber", "");

    OrderPayment payment = orderPaymentRepository.findByReference(paymentReference)
        .orElseThrow(() -> {
          log.error("Transaction not found | SahayPayClient | handleSahayPayCallback | reference={}",
              paymentReference);
          return new ResourceNotFoundException("OrderPayment", paymentReference);
        });

    if (PaymentStatus.SUCCESS == payment.getStatus()) {
      log.warn("Transaction already completed | SahayPayClient | handleSahayPayCallback | reference={}",
          paymentReference);
      throw new DuplicateTransactionException(paymentReference);
    }

    payment.setGatewayReference(gatewayReference);
    payment.recordWebhook(requestBody.toString(), response);
    orderPaymentRepository.save(payment);

    // Validate critical fields
    if (payment.getAmount().compareTo(callbackAmount) != 0 ||
        payment.getAccountNumber() == null ||
        !payment.getAccountNumber().equals(callbackPhoneNumber)) {
      log.error("Mismatch in amount or account number | SahayPayClient | handleSahayPayCallback | reference={}, expectedAmount={}, callbackAmount={}, expectedPhone={}, callbackPhone={}",
          paymentReference, payment.getAmount(), callbackAmount, payment.getAccountNumber(), callbackPhoneNumber);
      throw new PaymentProcessingException(
          String.format("Amount or account number mismatch for transaction %s", paymentReference));
    }

    if (!"000".equals(response)) {
      payment.resolve(PaymentStatus.FAILED, responseDescription);
      orderPaymentRepository.save(payment);

      log.error("Transaction failed | SahayPayClient | handleSahayPayCallback | reference={}, reason={}",
          paymentReference, responseDescription);

      throw new PaymentProcessingException(
          String.format("Transaction %s failed: %s", paymentReference, responseDescription));
    }

    // Success case
    payment.resolve(PaymentStatus.SUCCESS, "SUCCESS");
    orderPaymentRepository.save(payment);

    processSuccessPayment.pickAndProcess(payment);

    log.info("Transaction completed successfully | SahayPayClient | handleSahayPayCallback | reference={}",
        paymentReference);
  }

  // ==================== PRIVATE HELPER METHODS ====================

  private String authenticate() {
    JSONObject body = new JSONObject();
    body.put("username", config.getUsername());
    body.put("password", config.getPassword());

    String url = config.getBaseUrl() + "/auth/login";
    Map<String, String> headers = new java.util.HashMap<>(Map.of(
        "Content-Type", "application/json",
        "User-Agent", UserAgentProvider.get()
    ));


//    // Add these critical headers for Cloudflare
//    headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
//    headers.put("Accept-Language", "en-US,en;q=0.5");
//    headers.put("Accept-Encoding", "gzip, deflate, br");
//    headers.put("Connection", "keep-alive");
//    headers.put("Upgrade-Insecure-Requests", "1");
//    headers.put("Sec-Fetch-Dest", "document");
//    headers.put("Sec-Fetch-Mode", "navigate");
//    headers.put("Sec-Fetch-Site", "none");
//    headers.put("Sec-Fetch-User", "?1");
//    headers.put("Cache-Control", "max-age=0");
//    // Add Referer if you have a previous page
//    headers.put("Referer", config.getBaseUrl() + "/");
//    // If making API calls, sometimes Origin helps
//    headers.put("Origin", config.getBaseUrl());

    JSONObject resp = httpProcessor.executeStructuredRequest(url, "POST", body.toString(), headers).join();

    String statusCode = resp.optString("StatusCode");
    if (!"200".equals(statusCode) && !"201".equals(statusCode)) {
      log.error("Communication error | SahayPayClient | authenticate | service={}, response={}",
          SERVICE_NAME, resp.toString());
      throw new PaymentProcessingException(
          "SahayPay service temporarily unavailable. Please try again.");
    }

    String responseBodyStr = resp.optString("ResponseBody");
    JSONObject responseBody = new JSONObject(responseBodyStr);

    String responseCode = responseBody.optString("response");
    if (!"000".equals(responseCode)) {
      String errorMsg = responseBody.optString("responseDescription",
          "Unknown error from SahayPay");
      log.error("Business error | SahayPayClient | authenticate | code={}, message={}, response={}",
          responseCode, errorMsg, responseBodyStr);
      throw new PaymentProcessingException("SahayPay payment failed: " + errorMsg);
    }

    return responseBody.getString("accessToken");
  }

  private String validateAndFormatPhoneNumber(String phoneNumber) {
    if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
      throw new IllegalArgumentException("Phone number cannot be null or empty");
    }

    String digitsOnly = phoneNumber.replaceAll("\\D", "");

    if (digitsOnly.length() < 9) {
      throw new IllegalArgumentException(
          "Phone number must have at least 9 digits. Provided: " + digitsOnly);
    }

    String lastNineDigits = digitsOnly.substring(digitsOnly.length() - 9);
    return "251" + lastNineDigits;
  }

}
