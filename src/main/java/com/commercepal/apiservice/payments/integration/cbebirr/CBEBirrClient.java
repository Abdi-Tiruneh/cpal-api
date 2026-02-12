package com.commercepal.apiservice.payments.integration.cbebirr;

import com.commercepal.apiservice.orders.enums.PaymentStatus;
import com.commercepal.apiservice.payments.UserAgentProvider;
import com.commercepal.apiservice.payments.core.ProcessSuccessPayment;
import com.commercepal.apiservice.payments.oderPayment.OrderPayment;
import com.commercepal.apiservice.payments.oderPayment.OrderPaymentRepository;
import com.commercepal.apiservice.payments.oderPayment.enums.NextAction;
import com.commercepal.apiservice.payments.oderPayment.dto.PaymentInitiationResponse;
import com.commercepal.apiservice.shared.exceptions.resource.ResourceNotFoundException;
import com.commercepal.apiservice.utils.HttpProcessor;
import com.commercepal.apiservice.utils.ResponseCodes;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CBEBirrClient {

  private final HttpProcessor httpProcessor;
  private final CbeBirrConfig config;
  private final OrderPaymentRepository orderPaymentRepository;
  private final ProcessSuccessPayment processSuccessPayment;

  public PaymentInitiationResponse pickAndProcess(OrderPayment payment) {
    try {
      JSONObject reqBody = new JSONObject();
      reqBody.put("amount", payment.getAmount().toString());
      reqBody.put("transRef", payment.getReference());
      String bodyStr = reqBody.toString();

      payment.recordInitRequest(bodyStr);
      orderPaymentRepository.save(payment);

      Map<String, String> headers = Map.of(
          "Content-Type", "application/json",
          "User-Agent", UserAgentProvider.get()
      );

      log.info("Initiating CBE Birr payment | CBEBirrClient | pickAndProcess | reference={}, orderId={}",
          payment.getReference(), payment.getOrder() != null ? payment.getOrder().getId() : null);

      JSONObject resp = httpProcessor.executeStructuredRequest(
          config.getUtilService(),
          "POST",
          bodyStr,
          headers
      ).join();

      String statusCode = resp.optString("StatusCode", "0");
      String responseBodyStr = resp.optString("ResponseBody", "{}");

      payment.recordInitResponse(responseBodyStr);
      orderPaymentRepository.save(payment);

      if (!"200".equals(statusCode)) {
        log.error("CBE Birr util error | CBEBirrClient | pickAndProcess | reference={}, statusCode={}, response={}",
            payment.getReference(), statusCode, responseBodyStr);
        payment.resolve(PaymentStatus.FAILED, "Request failed");
        orderPaymentRepository.save(payment);
        return buildResponse(payment, false, null, "Request failed", NextAction.OPEN_ADDITIONAL_INPUT);
      }

      JSONObject resBody = new JSONObject(responseBodyStr);
      String requestParameters = resBody.optString("requestParameters", "").trim();

      if (requestParameters.isEmpty()) {
        log.warn("CBE Birr util missing requestParameters | CBEBirrClient | pickAndProcess | reference={}, response={}",
            payment.getReference(), responseBodyStr);
        payment.resolve(PaymentStatus.FAILED, "Invalid util response");
        orderPaymentRepository.save(payment);
        return buildResponse(payment, false, null, "Request failed", NextAction.OPEN_ADDITIONAL_INPUT);
      }

      String paymentUrl = config.getPaymentUrl() + requestParameters;
      payment.setGatewayReference(payment.getReference());
      payment.resolve(PaymentStatus.PENDING, "PENDING");
      orderPaymentRepository.save(payment);

      log.info("CBE Birr payment initiated successfully | CBEBirrClient | pickAndProcess | reference={}",
          payment.getReference());

      return buildResponse(
          payment,
          true,
          paymentUrl,
          "You will be redirected to complete your payment",
          NextAction.REDIRECT_TO_PAYMENT_URL
      );

    } catch (Exception ex) {
      log.warn("CBE Birr payment failed | CBEBirrClient | pickAndProcess | reference={}, error={}",
          payment.getReference(), ex.getMessage());
      payment.resolve(PaymentStatus.FAILED, ex.getMessage());
      orderPaymentRepository.save(payment);
      return buildResponse(payment, false, null, "Request failed", NextAction.OPEN_ADDITIONAL_INPUT);
    }
  }

  private PaymentInitiationResponse buildResponse(
      OrderPayment payment,
      boolean success,
      String paymentUrl,
      String instructions,
      NextAction nextAction
  ) {
    return PaymentInitiationResponse.builder()
        .success(success)
        .orderNumber(payment.getOrder() != null ? payment.getOrder().getOrderNumber() : null)
        .paymentReference(payment.getReference())
        .paymentUrl(paymentUrl)
        .paymentInstructions(instructions)
        .nextAction(nextAction)
        .build();
  }


  public JSONObject handleCallBack(JSONObject reqBdy) {
    JSONObject respBdy = new JSONObject();
    String transactionId = reqBdy.optString("TransactionId", "");
    String transactionState = reqBdy.optString("State", "");

    log.info("Processing CBE Birr payment fulfillment | CBEBirrPaymentFulfillment | pickAndProcess | transactionId={}, state={}",
        transactionId, transactionState);

    try {
      OrderPayment payment = orderPaymentRepository.findByReference(transactionId)
          .orElseThrow(() -> {
            log.warn("Transaction not found | CBEBirrPaymentFulfillment | pickAndProcess | transactionId={}",
                transactionId);
            return new ResourceNotFoundException("OrderPayment", transactionId);
          });

      // Check if already processed
      if (PaymentStatus.SUCCESS == payment.getStatus()) {
        log.warn("Transaction already completed | CBEBirrPaymentFulfillment | pickAndProcess | transactionId={}",
            transactionId);
        return respBdy.put("statusCode", ResponseCodes.BAD_REQUEST)
            .put("statusDescription", "Failed")
            .put("statusMessage", "The transaction has already been processed successfully.");
      }

      // Record webhook payload
      payment.recordWebhook(reqBdy.toString(), transactionState);

      // Set gateway reference from transactionId if available
      if (transactionId != null && !transactionId.isEmpty()) {
        payment.setGatewayReference(transactionId);
      }
      orderPaymentRepository.save(payment);

      if ("Success".equalsIgnoreCase(transactionState)) {
        payment.resolve(PaymentStatus.SUCCESS, "SUCCESS");
        orderPaymentRepository.save(payment);

        // Trigger downstream fulfillment
        try {
          processSuccessPayment.pickAndProcess(payment);
          log.info("Post-payment workflow triggered successfully | CBEBirrPaymentFulfillment | pickAndProcess | transactionId={}",
              transactionId);
        } catch (Exception e) {
          log.error("Post-payment workflow failed | CBEBirrPaymentFulfillment | pickAndProcess | transactionId={}, error={}",
              transactionId, e.getMessage(), e);
        }

        log.info("Transaction processed successfully | CBEBirrPaymentFulfillment | pickAndProcess | transactionId={}",
            transactionId);

        respBdy.put("statusCode", ResponseCodes.SUCCESS)
            .put("billRef", payment.getOrder() != null ? payment.getOrder().getOrderNumber() : null)
            .put("TransRef", payment.getReference())
            .put("statusDescription", "Transaction Successful")
            .put("statusMessage", "The transaction has been successfully completed.");

      } else {
        String failureMessage = transactionState != null && !transactionState.isEmpty()
            ? transactionState : "FAILED";
        log.warn("Transaction failed | CBEBirrPaymentFulfillment | pickAndProcess | transactionId={}, state={}",
            transactionId, transactionState);

        payment.setGatewayReference("FAILED");
        payment.resolve(PaymentStatus.FAILED, failureMessage);
        orderPaymentRepository.save(payment);

        respBdy.put("statusCode", ResponseCodes.BAD_REQUEST)
            .put("statusDescription", "Failed")
            .put("statusMessage", "Transaction could not be completed.");
      }

      return respBdy;

    } catch (ResourceNotFoundException e) {
      log.error("Transaction not found | CBEBirrPaymentFulfillment | pickAndProcess | transactionId={}, error={}",
          transactionId, e.getMessage());
      return respBdy.put("statusCode", ResponseCodes.NOT_FOUND)
          .put("statusDescription", "Failed")
          .put("statusMessage", "Transaction could not be found.");
    } catch (Exception ex) {
      log.error("Error processing CBE Birr payment fulfillment | CBEBirrPaymentFulfillment | pickAndProcess | transactionId={}, error={}",
          transactionId, ex.getMessage(), ex);
      return respBdy.put("statusCode", ResponseCodes.INTERNAL_SERVER_ERROR)
          .put("statusDescription", "Request failed")
          .put("statusMessage", "Failed");
    }
  }
}
