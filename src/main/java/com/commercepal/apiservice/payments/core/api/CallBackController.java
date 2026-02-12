//package com.commercepal.apiservice.payments.core.api;
//
//import com.commerce.pal.payment.integ.payment.telebirr_ussd.TelebirrUssdCallbackParser;
//import com.commerce.pal.payment.integ.payment.telebirr_ussd.TelebirrUssdCallbackRequest;
//import com.commerce.pal.payment.integ.payment.telebirr_ussd.TelebirrUssdPaymentFulfillmentService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.json.JSONObject;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.CrossOrigin;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RequestMapping("/api/v1/payments")
//@Slf4j
//@RestController
//@CrossOrigin(origins = "*")
//@RequiredArgsConstructor
//public class CallBackController {
//
//  private final TelebirrUssdCallbackParser telebirrUssdCallbackParser;
//  private final TelebirrUssdPaymentFulfillmentService telebirrUssdPaymentFulfillmentService;
//
//
//  @PostMapping(value = "/telebirr", produces = MediaType.APPLICATION_JSON_VALUE)
//  public ResponseEntity<String> postTeleBirRes(@RequestBody String requestBody) {
//    JSONObject responseBody = new JSONObject();
//
//    try {
//      // Pretty print request payload
//      JSONObject payload = new JSONObject(requestBody);
//
//      log.info("📩 Incoming TeleBirr Callback:\n{}", new JSONObject()
//          .put("event", "TeleBirrCallback")
//          .put("status", "RECEIVED")
//          .put("payload", payload)
//          .toString(4) // ✅ pretty-print with 4 spaces indentation
//      );
//
//      responseBody.put("code", 0).put("msg", "Success");
//
//      // Pretty print success response
//      log.info("✅ TeleBirr Processing Success:\n{}", new JSONObject()
//          .put("event", "TeleBirrCallback")
//          .put("status", "SUCCESS")
//          .put("response", responseBody)
//          .toString(4));
//
//    } catch (Exception ex) {
//      responseBody.put("code", 5).put("msg", "Failed");
//
//      // Pretty print failure response
//      log.error("❌ TeleBirr Processing Failed:\n{}", new JSONObject()
//          .put("event", "TeleBirrCallback")
//          .put("status", "FAILED")
//          .put("errorMessage", ex.getMessage())
//          .put("payload", requestBody)
//          .toString(4), ex);
//    }
//
//    return ResponseEntity.ok(responseBody.toString());
//  }
//
//  @PostMapping(value = "/teleBirrUssd/callback", produces = MediaType.APPLICATION_JSON_VALUE)
//  public ResponseEntity<String> handleCallback(
//      @RequestBody String xmlPayload) {
//    log.info("📩 [Telebirr USSD Callback Received] -------------------------");
//    log.info("📝 Raw XML Payload:\n{}", xmlPayload);
//
//    JSONObject response = new JSONObject();
//
//    try {
//      // 1. Parse the callback XML payload
//      TelebirrUssdCallbackRequest callback = telebirrUssdCallbackParser.parseCallback(xmlPayload);
//
//      log.info("✅ [Callback Parsed Successfully] ---------------------");
//      log.info("🔑 TransactionID     : {}", callback.getTransactionId());
//      log.info("🗨️ ConversationID    : {}", callback.getConversationId());
//      log.info("📡 CpalTransactionRef  : {}", callback.getCpalTransactionRef());
//
//      log.info("🎯 Result ---------------------------");
//      log.info("   • ResultCode : {}", callback.getResultCode());
//      log.info("   • ResultType : {}", callback.getResultType());
//      log.info("   • Description: {}", callback.getResultDesc());
//
//      // 2. Check if transaction is successful
//      if (callback.getIsSuccess()) {
//        log.info("🏆 Transaction Status: ✅ SUCCESS --------------------");
//      } else {
//        log.warn("🚨 Transaction Status: ❌ FAILED ---------------------");
//        // TODO: Send failure notifications to customer
//      }
//
//      response = telebirrUssdPaymentFulfillmentService.updateTransactionStatus(
//          callback);
//
//      log.info("------------------------------------------------------");
//
//    } catch (Exception e) {
//      log.error("💥 Error processing callback --------------------------", e);
//      response.put("statusCode", "500")
//          .put("statusDescription", "Failed")
//          .put("statusMessage", "Internal system error.");
//    }
//
//    return ResponseEntity.ok(response.toString());
//  }
//
//}
