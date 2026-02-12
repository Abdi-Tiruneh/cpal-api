package com.commercepal.apiservice.payments.integration.amole;

import com.commercepal.apiservice.orders.enums.PaymentStatus;
import com.commercepal.apiservice.payments.core.ProcessSuccessPayment;
import com.commercepal.apiservice.payments.oderPayment.OrderPayment;
import com.commercepal.apiservice.payments.oderPayment.OrderPaymentRepository;
import com.commercepal.apiservice.payments.oderPayment.enums.NextAction;
import com.commercepal.apiservice.payments.oderPayment.dto.PaymentInitiationResponse;
import com.commercepal.apiservice.utils.HttpProcessor;
import com.commercepal.apiservice.utils.ResponseCodes;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AmoleClient {

  private final AmoleConfig config;
  private final HttpProcessor httpProcessor;
  private final OrderPaymentRepository orderPaymentRepository;
  private final ProcessSuccessPayment processSuccessPayment;

  public PaymentInitiationResponse pickAndProcess(OrderPayment payment) {
    log.info("Initiating Amole payment | AmoleClient | pickAndProcess | reference={}, orderId={}",
        payment.getReference(), payment.getOrder() != null ? payment.getOrder().getId() : null);

    try {
      String encodedData = "BODY_CardNumber=" + payment.getAccountNumber() +
          "&BODY_PaymentAction=" + config.getPayment().getAction().getAuthorization() +
          "&BODY_AmountX=" + payment.getAmount() +
          "&BODY_AmoleMerchantID=" + config.getAuthenticate().getAmoleMerchantID() +
          "&BODY_OrderDescription=" + "payment for commercepal.com" +
          "&BODY_SourceTransID=" + payment.getReference() +
          "&BODY_VendorAccount=" + config.getTin();

      payment.recordInitRequest(encodedData);
      orderPaymentRepository.save(payment);

      Map<String, String> headers = Map.of(
          "HDR_Signature", config.getSignature(),
          "HDR_IPAddress", config.getIpAddress(),
          "HDR_UserName", config.getAuthenticate().getUserName(),
          "HDR_Password", config.getAuthenticate().getPassword(),
          "Content-Type", "application/x-www-form-urlencoded"
      );

      JSONObject resp = httpProcessor.executeStructuredRequest(
          config.getPayment().getUrl(),
          "POST",
          encodedData,
          headers
      ).join();

      String statusCode = resp.optString("StatusCode", "0");
      String responseBodyStr = resp.optString("ResponseBody", "{}");

      if ("200".equals(statusCode)) {
        JSONArray responseBodyArray = new JSONArray(responseBodyStr);
        JSONObject resBody = responseBodyArray.getJSONObject(0);

        //cant save payload as request payload, bcuz it`s too large
        JSONObject resBodyToSave = new JSONObject();
        resBodyToSave
            .put("HDR_ResponseID", resBody.opt("HDR_ResponseID"))
            .put("HDR_Acknowledge", resBody.opt("HDR_Acknowledge"))
            .put("HDR_SourceTransID", resBody.opt("HDR_SourceTransID"))
            .put("BODY_AuthorizationCode", resBody.opt("BODY_AuthorizationCode"))
            .put("BODY_CardNumber", resBody.opt("BODY_CardNumber"))
            .put("BODY_Amount", resBody.opt("BODY_Amount"))
            .put("BODY_PaymentAction", resBody.opt("BODY_PaymentAction"))
            .put("MSG_ErrorCode", resBody.opt("MSG_ErrorCode"))
            .put("MSG_ShortMessage", resBody.opt("MSG_ShortMessage"));

        payment.recordInitResponse(resBodyToSave.toString());

        String errorCode = resBody.optString("MSG_ErrorCode", "");
        if ("00001".equals(errorCode)) {
          payment.setGatewayReference(payment.getReference());
          payment.resolve(PaymentStatus.PENDING, "PENDING");
          orderPaymentRepository.save(payment);

          log.info("Amole payment authorization successful | AmoleClient | pickAndProcess | reference={}",
              payment.getReference());

          return PaymentInitiationResponse.builder()
              .success(true)
              .orderNumber(payment.getOrder() != null ? payment.getOrder().getOrderNumber() : null)
              .paymentReference(payment.getReference())
              .paymentUrl(null)
              .paymentInstructions("Success")
              .nextAction(NextAction.OPEN_ADDITIONAL_INPUT)
              .build();
        } else {
          String longMessage = resBody.optString("MSG_LongMessage", "Payment authorization failed");
          log.warn("Amole payment business error | AmoleClient | pickAndProcess | reference={}, errorCode={}, message={}",
              payment.getReference(), errorCode, longMessage);

          // don't update payment status. give user another chance
          payment.setProviderFinalMessage(longMessage);
          orderPaymentRepository.save(payment);

          return PaymentInitiationResponse.builder()
              .success(true)
              .orderNumber(payment.getOrder() != null ? payment.getOrder().getOrderNumber() : null)
              .paymentReference(payment.getReference())
              .paymentUrl(null)
              .paymentInstructions(longMessage)
              .nextAction(NextAction.OPEN_ADDITIONAL_INPUT)
              .build();
        }
      } else {
        log.error("Amole payment API error | AmoleClient | pickAndProcess | reference={}, statusCode={}, response={}",
            payment.getReference(), statusCode, responseBodyStr);

        payment.setGatewayReference("FAILED");
        payment.recordInitResponse("FAILED");
        payment.resolve(PaymentStatus.FAILED, "FAILED");
        orderPaymentRepository.save(payment);

        return PaymentInitiationResponse.builder()
            .success(true)
            .orderNumber(payment.getOrder() != null ? payment.getOrder().getOrderNumber() : null)
            .paymentReference(payment.getReference())
            .paymentUrl(null)
            .paymentInstructions("Request failed")
            .nextAction(NextAction.OPEN_ADDITIONAL_INPUT)
            .build();
      }

    } catch (Exception ex) {
      log.error("Amole payment failed | AmoleClient | pickAndProcess | reference={}, error={}",
          payment.getReference(), ex.getMessage(), ex);
      payment.resolve(PaymentStatus.FAILED, ex.getMessage());
      orderPaymentRepository.save(payment);
      return PaymentInitiationResponse.builder()
          .success(true)
          .orderNumber(payment.getOrder() != null ? payment.getOrder().getOrderNumber() : null)
          .paymentReference(payment.getReference())
          .paymentUrl(null)
          .paymentInstructions("Request failed")
          .nextAction(NextAction.OPEN_ADDITIONAL_INPUT)
          .build();
    }
  }


  public JSONObject pickAndProcess(JSONObject reqBdy) {
    JSONObject respBdy = new JSONObject();
    String transRef = reqBdy.optString("TransRef", "");

    log.info("Processing Amole payment fulfillment | AmolePaymentFulfillment | pickAndProcess | transRef={}",
        transRef);

    try {
      orderPaymentRepository.findByReferenceAndStatus(transRef, PaymentStatus.PENDING)
          .ifPresentOrElse(payment -> {
            String encodedData = "BODY_CardNumber=" + payment.getAccountNumber() +
                "&BODY_PIN=" + reqBdy.getString("OTP") +
                "&BODY_PaymentAction=" + config.getPayment().getAction().getPayment() +
                "&BODY_AmountX=" + payment.getAmount() +
                "&BODY_AmoleMerchantID=" + config.getAuthenticate().getAmoleMerchantID() +
                "&BODY_OrderDescription=" + "payment for commercepal.com" +
                "&BODY_SourceTransID=" + payment.getReference() +
                "&BODY_VendorAccount=" + config.getTin();

            payment.recordInitRequest(encodedData);
            orderPaymentRepository.save(payment);

            Map<String, String> headers = Map.of(
                "HDR_Signature", config.getSignature(),
                "HDR_IPAddress", config.getIpAddress(),
                "HDR_UserName", config.getAuthenticate().getUserName(),
                "HDR_Password", config.getAuthenticate().getPassword(),
                "Content-Type", "application/x-www-form-urlencoded"
            );

            log.info("Sending Amole payment request | AmolePaymentFulfillment | pickAndProcess | reference={}, orderId={}",
                payment.getReference(), payment.getOrder() != null ? payment.getOrder().getId() : null);

            JSONObject resp = httpProcessor.executeStructuredRequest(
                config.getPayment().getUrl(),
                "POST",
                encodedData,
                headers
            ).join();

            String statusCode = resp.optString("StatusCode", "0");
            String responseBodyStr = resp.optString("ResponseBody", "{}");

            if ("200".equals(statusCode)) {
              JSONArray responseBodyArray = new JSONArray(responseBodyStr);
              JSONObject resBody = responseBodyArray.getJSONObject(0);

              //cant save payload as request payload, bcuz it`s too large
              JSONObject resBodyToSave = new JSONObject();
              resBodyToSave
                  .put("HDR_ResponseID", resBody.opt("HDR_ResponseID"))
                  .put("HDR_SourceTransID", resBody.opt("HDR_SourceTransID"))
                  .put("BODY_CardNumber", resBody.opt("BODY_CardNumber"))
                  .put("BODY_Amount", resBody.opt("BODY_Amount"))
                  .put("BODY_PaymentAction", resBody.opt("BODY_PaymentAction"))
                  .put("MSG_ErrorCode", resBody.opt("MSG_ErrorCode"))
                  .put("MSG_ShortMessage", resBody.opt("MSG_ShortMessage"));

              payment.recordInitResponse(resBodyToSave.toString());
              orderPaymentRepository.save(payment);

              String errorCode = resBody.optString("MSG_ErrorCode", "");
              if ("00001".equals(errorCode)) {
                LocalDate currentDate = LocalDate.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
                String formattedDate = currentDate.format(formatter);

                String amoleTransRef = resBody.optString("HDR_ReferenceNumber", "");
                String longMessage = resBody.optString("MSG_LongMessage", "Success");

                respBdy.put("statusCode", ResponseCodes.SUCCESS)
                    .put("statusDescription", longMessage)
                    .put("statusMessage", "Success")
                    .put("OrderRef", payment.getOrder() != null ? payment.getOrder().getOrderNumber() : null)
                    .put("transRef", payment.getReference())
                    .put("amoleTransRef", amoleTransRef)
                    .put("amount", payment.getAmount())
                    .put("phoneNumber", payment.getAccountNumber())
                    .put("transDate", formattedDate);

                payment.setGatewayReference(amoleTransRef);
                payment.resolve(PaymentStatus.SUCCESS, "SUCCESS");
                orderPaymentRepository.save(payment);

                log.info("Amole payment successful | AmolePaymentFulfillment | pickAndProcess | reference={}, amoleTransRef={}",
                    payment.getReference(), amoleTransRef);

                // Process Payment
                processSuccessPayment.pickAndProcess(payment);
              } else {
                String longMessage = resBody.optString("MSG_LongMessage", "Payment failed");
                respBdy.put("statusCode", ResponseCodes.BAD_REQUEST)
                    .put("statusDescription", longMessage)
                    .put("statusMessage", "Failed");

                log.warn("Amole payment business error | AmolePaymentFulfillment | pickAndProcess | reference={}, errorCode={}, message={}",
                    payment.getReference(), errorCode, longMessage);

                // don't update payment status. give user another chance
                payment.setProviderFinalMessage(longMessage);
                orderPaymentRepository.save(payment);
              }
            } else {
              log.error("Amole payment API error | AmolePaymentFulfillment | pickAndProcess | reference={}, statusCode={}, response={}",
                  payment.getReference(), statusCode, responseBodyStr);

              respBdy.put("statusCode", ResponseCodes.BAD_REQUEST)
                  .put("statusDescription", "Request failed")
                  .put("statusMessage", "Failed");

              payment.recordInitResponse("FAILED");
              payment.setGatewayReference("FAILED");
              payment.resolve(PaymentStatus.FAILED, "FAILED");
              orderPaymentRepository.save(payment);
            }
          }, () -> {
            log.warn("Amole payment not found | AmolePaymentFulfillment | pickAndProcess | transRef={}",
                transRef);
            respBdy.put("statusCode", ResponseCodes.NOT_FOUND)
                .put("statusDescription", "Failed")
                .put("statusMessage", "Payment not found");
          });
    } catch (Exception ex) {
      log.error("Amole payment fulfillment error | AmolePaymentFulfillment | pickAndProcess | transRef={}, error={}",
          transRef, ex.getMessage(), ex);
      respBdy.put("statusCode", ResponseCodes.INTERNAL_SERVER_ERROR)
          .put("statusDescription", "Request failed")
          .put("statusMessage", "Failed");
    }
    return respBdy;
  }
}
