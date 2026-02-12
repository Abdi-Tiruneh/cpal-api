package com.commercepal.apiservice.payments.integration.ebirr;

import com.commercepal.apiservice.orders.enums.PaymentStatus;
import com.commercepal.apiservice.payments.UserAgentProvider;
import com.commercepal.apiservice.payments.core.ProcessSuccessPayment;
import com.commercepal.apiservice.payments.oderPayment.OrderPayment;
import com.commercepal.apiservice.payments.oderPayment.OrderPaymentRepository;
import com.commercepal.apiservice.payments.oderPayment.enums.NextAction;
import com.commercepal.apiservice.payments.oderPayment.dto.PaymentInitiationResponse;
import com.commercepal.apiservice.utils.HttpProcessor;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EbirrClient {

  private final HttpProcessor httpProcessor;
  private final EbirrConfig config;
  private final OrderPaymentRepository orderPaymentRepository;
  private final ProcessSuccessPayment processSuccessPayment;

  public PaymentInitiationResponse pickAndProcess(OrderPayment payment) {
    try {
      String formattedPhone = formatPhoneNumber(payment.getAccountNumber(), payment.getGateway());
      payment.setAccountNumber(formattedPhone);

      JSONObject payload = buildPayload(payment);
      String payloadStr = payload.toString();

      payment.recordInitRequest(payloadStr);
      orderPaymentRepository.save(payment);

      Map<String, String> headers = Map.of(
          "Content-Type", "application/json",
          "User-Agent", UserAgentProvider.get()
      );

      log.info("Initiating eBirr payment | EbirrClient | pickAndProcess | reference={}, orderId={}",
          payment.getReference(), payment.getOrder() != null ? payment.getOrder().getId() : null);

      JSONObject resp = httpProcessor.executeStructuredRequest(
          config.getInitiatePayment(),
          "POST",
          payloadStr,
          headers
      ).join();

      String statusCode = resp.optString("StatusCode", "0");
      String responseBodyStr = resp.optString("ResponseBody", "{}");

      payment.recordInitResponse(responseBodyStr);
      orderPaymentRepository.save(payment);

      if (!"200".equals(statusCode)) {
        log.error("eBirr API error | EbirrClient | pickAndProcess | reference={}, statusCode={}, response={}",
            payment.getReference(), statusCode, responseBodyStr);
        payment.resolve(PaymentStatus.FAILED, "Service communication failed");
        orderPaymentRepository.save(payment);
        return buildResponse(payment, false, "Request failed");
      }

      JSONObject resBody = new JSONObject(responseBodyStr);
      String responseCode = resBody.optString("responseCode", "");
      String responseMsg = resBody.optString("responseMsg", "Unknown error");

      if ("2001".equals(responseCode)) {
        JSONObject paramsBdy = resBody.optJSONObject("params");
        String transactionId = paramsBdy != null ? paramsBdy.optString("transactionId", "") : "";
        String issuerTransactionId = paramsBdy != null ? paramsBdy.optString("issuerTransactionId", "") : "";

        payment.setGatewayReference(transactionId);
        payment.resolve(PaymentStatus.SUCCESS, responseMsg + " - " + issuerTransactionId);
        orderPaymentRepository.save(payment);

        processSuccessPayment.pickAndProcess(payment);

        log.info("eBirr payment initiated successfully | EbirrClient | pickAndProcess | reference={}, transactionId={}",
            payment.getReference(), transactionId);

        return buildResponse(payment, true, "Success");
      }

      log.warn("eBirr business error | EbirrClient | pickAndProcess | reference={}, code={}, message={}",
          payment.getReference(), responseCode, responseMsg);
      payment.setGatewayReference("FAILED");
      payment.resolve(PaymentStatus.FAILED, responseMsg);
      orderPaymentRepository.save(payment);

      return buildResponse(payment, false, responseMsg);

    } catch (Exception ex) {
      log.warn("eBirr payment failed | EbirrClient | pickAndProcess | reference={}, error={}",
          payment.getReference(), ex.getMessage());
      payment.resolve(PaymentStatus.FAILED, ex.getMessage());
      orderPaymentRepository.save(payment);
      return buildResponse(payment, false, "Request failed");
    }
  }

  private String formatPhoneNumber(String accountNumber, String gateway) {
    if (accountNumber == null || accountNumber.trim().isEmpty()) {
      throw new IllegalArgumentException("Account number cannot be null or empty");
    }
    String digitsOnly = accountNumber.replaceAll("\\D", "");
    String nineDigits = digitsOnly.length() > 9
        ? digitsOnly.substring(digitsOnly.length() - 9)
        : digitsOnly;

    if ("KAAFI_EBIRR".equals(gateway)) {
      return "231438251" + nineDigits;
    }
    return "0" + nineDigits;
  }

  private JSONObject buildPayload(OrderPayment payment) {
    JSONObject payload = new JSONObject();
    payload.put("schemaVersion", config.getSchemaVersion());
    payload.put("requestId", payment.getReference());
    payload.put("timestamp", payment.getReference());
    payload.put("channelName", config.getChannelName());
    payload.put("serviceName", config.getServiceName());

    JSONObject serviceParams = new JSONObject();
    serviceParams.put("merchantUid", config.getMerchantUid());
    serviceParams.put("paymentMethod", config.getPaymentMethod());
    serviceParams.put("apiKey", config.getApiKey());
    serviceParams.put("apiUserId", config.getApiUserId());

    JSONObject payerInfo = new JSONObject();
    payerInfo.put("accountNo", payment.getAccountNumber());
    serviceParams.put("payerInfo", payerInfo);

    JSONObject transactionInfo = new JSONObject();
    transactionInfo.put("amount", payment.getAmount().toString());
    transactionInfo.put("currency", "ETB");
    transactionInfo.put("description", "Payment for Order:" + (payment.getOrder() != null ? payment.getOrder().getOrderNumber() : payment.getReference()));
    transactionInfo.put("referenceId", payment.getReference());
    transactionInfo.put("invoiceId", "I" + payment.getReference());

    serviceParams.put("transactionInfo", transactionInfo);
    payload.put("serviceParams", serviceParams);

    return payload;
  }

  private PaymentInitiationResponse buildResponse(OrderPayment payment, boolean success, String instructions) {
    return PaymentInitiationResponse.builder()
        .success(success)
        .orderNumber(payment.getOrder() != null ? payment.getOrder().getOrderNumber() : null)
        .paymentReference(payment.getReference())
        .paymentUrl(null)
        .paymentInstructions(instructions)
        .nextAction(NextAction.OPEN_ADDITIONAL_INPUT)
        .build();
  }
}
