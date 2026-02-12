//package com.commercepal.apiservice.payments.core.api;
//
//import com.commerce.pal.payment.integ.payment.amole.AmolePaymentFulfillment;
//import com.commerce.pal.payment.integ.payment.cash.AgentCashProcessing;
//import com.commerce.pal.payment.integ.payment.cbeBirrMiniApp.CBEBirrMiniAppPaymentFulfillment;
//import com.commerce.pal.payment.integ.payment.cbebirr.CBEBirrPaymentFulfillment;
//import com.commerce.pal.payment.integ.payment.epg.EPGPaymentFulfillment;
//import com.commerce.pal.payment.integ.payment.ethio.EthioSwithAccount;
//import com.commerce.pal.payment.integ.payment.financials.hilalPay.HalalPayLoanCancel;
//import com.commerce.pal.payment.integ.payment.financials.hilalPay.HalalPayLoanRequest;
//import com.commerce.pal.payment.integ.payment.financials.hilalPay.HilalPayLoanConfirmation;
//import com.commerce.pal.payment.integ.payment.financials.rays.RaysLoanConfirmation;
//import com.commerce.pal.payment.integ.payment.financials.rays.RaysLoanRequest;
//import com.commerce.pal.payment.integ.payment.hellocash.HelloCashPaymentFulfillment;
//import com.commerce.pal.payment.integ.payment.ipay.IPayPaymentFulfillment;
//import com.commerce.pal.payment.integ.payment.sahay.SahayPaymentFulfillment;
//import com.commerce.pal.payment.integ.payment.sahayv2.SahayPayClient;
//import com.commerce.pal.payment.integ.payment.telebirr.TeleBirrPaymentFulfillment;
//import com.commerce.pal.payment.integ.payment.telebirr_mini_app.TeleBirrMiniAppPaymentFulfillment;
//import com.commerce.pal.payment.jms.Sender;
//import com.commerce.pal.payment.module.ValidateAccessToken;
//import com.commerce.pal.payment.module.payment.PaymentService;
//import com.commerce.pal.payment.module.payment.PromotionService;
//import com.commerce.pal.payment.util.GlobalMethods;
//import com.commerce.pal.payment.util.ResponseCodes;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.json.JSONObject;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.CrossOrigin;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestHeader;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//
//@RequestMapping("/payment/v1")
//@Slf4j
//@RestController
//@CrossOrigin(origins = "*")
//@RequiredArgsConstructor
//public class RequestController {
//
//  private final Sender sender;
//  private final GlobalMethods globalMethods;
//  private final PaymentService paymentService;
//  private final PromotionService promotionService;
//  private final EthioSwithAccount ethioSwithAccount;
//  private final ValidateAccessToken validateAccessToken;
//  private final AgentCashProcessing agentCashProcessing;
//  //    private final SahayCustomerValidation sahayCustomerValidation;
//  private final SahayPaymentFulfillment sahayPaymentFulfillment;
//  private final TeleBirrPaymentFulfillment teleBirrPaymentFulfillment;
//  private final HelloCashPaymentFulfillment helloCashPaymentFulfillment;
//  private final CBEBirrPaymentFulfillment cbeBirrPaymentFulfillment;
//  private final EPGPaymentFulfillment epgPaymentFulfillment;
//  private final AmolePaymentFulfillment amolePaymentFulfillment;
//  private final CBEBirrMiniAppPaymentFulfillment cbeBirrMiniAppPaymentFulfillment;
//  private final RaysLoanConfirmation raysLoanConfirmation;
//  private final RaysLoanRequest raysLoanRequest;
//  private final HilalPayLoanConfirmation hilalPayLoanConfirmation;
//  private final HalalPayLoanRequest halalPayLoanRequest;
//  private final IPayPaymentFulfillment iPayPaymentFulfillment;
//  private final HalalPayLoanCancel halalPayLoanCancel;
//  private final TeleBirrMiniAppPaymentFulfillment teleBirrMiniAppPaymentFulfillment;
//  private final SahayPayClient sahayPayClient;
//
//  @PostMapping(value = "/request", produces = MediaType.APPLICATION_JSON_VALUE)
//  public ResponseEntity<String> postRequest(
//      @RequestBody String requestBody,
//      @RequestHeader("Authorization") String accessToken
//  ) {
//    log.info(requestBody);
//    JSONObject responseBody = new JSONObject();
//
//    JSONObject requestObject = new JSONObject(requestBody);
//    Boolean checkToken = true;
//
//    switch (requestObject.getString("ServiceCode")) {
//      case "SAHAY-LOOKUP":
//        checkToken = false;
//        responseBody = sahayPayClient.checkCustomer(requestObject.getString("PhoneNumber"));
//        break;
//      case "ES-BANK-LOOKUP":
//        checkToken = false;
//        responseBody = ethioSwithAccount.bankCheck();
//        break;
//      case "ES-ACCOUNT-LOOKUP":
//        checkToken = false;
//        responseBody = ethioSwithAccount.accountCheck(requestObject);
//        break;
//    }
//    if (checkToken.equals(true)) {
//      JSONObject valTokenReq = new JSONObject();
//      valTokenReq.put("AccessToken", accessToken)
//          .put("UserType", requestObject.getString("UserType"));
//      JSONObject valTokenBdy = validateAccessToken.pickAndReturnAll(valTokenReq);
//
//      if (valTokenBdy.getString("Status").equals("00")) {
//        requestObject.put("UserEmail", valTokenBdy.getString("Email"));
//        requestObject.put("UserId", globalMethods.getUserId(requestObject.getString("UserType"),
//            valTokenBdy.getJSONObject("UserDetails")));
//        JSONObject userDetailsDetails = valTokenBdy.getJSONObject("UserDetails")
//            .getJSONObject("Details");
//        requestObject.put("UserLanguage", userDetailsDetails.getString("language"));
//        requestObject.put("CustomerCommissionAccount",
//            userDetailsDetails.getString("customerCommissionAccount"));
//
//        switch (requestObject.getString("ServiceCode")) {
//          case "ORDER-PROMO":
//            responseBody = promotionService.pickAndProcess(requestObject);
//            break;
//          case "CHECKOUT":
//          case "LOAN-REQUEST":
//            responseBody = paymentService.pickAndProcess(requestObject);
//            break;
//          case "SAHAY-CONFIRM-PAYMENT":
//            responseBody = sahayPaymentFulfillment.pickAndProcess(requestObject);
//            break;
//          case "HELLO-CASH-CONFIRM-PAYMENT":
//            responseBody = helloCashPaymentFulfillment.pickAndProcess(requestObject);
//            break;
//          case "AGENT-CASH-FULFILLMENT":
//            responseBody = agentCashProcessing.processFulfillment(requestObject);
//            break;
//          default:
//            responseBody
//                .put("statusCode", ResponseCodes.REQUEST_NOT_ACCEPTED)
//                .put("statusDescription", "Request failed")
//                .put("statusMessage", "Invalid service code");
//            break;
//        }
//      } else {
//        responseBody
//            .put("statusCode", ResponseCodes.REQUEST_NOT_ACCEPTED)
//            .put("statusDescription", "Request failed")
//            .put("statusMessage", "Failed to validate token.");
//      }
//    }
//    return ResponseEntity.ok(responseBody.toString());
//  }
//
//  @GetMapping(value = "/rays/markups", produces = MediaType.APPLICATION_JSON_VALUE)
//  public ResponseEntity<String> getRaysMarkups() {
//    String response = raysLoanRequest.getRaysMarkups();
//    return ResponseEntity.ok(response);
//  }
//
//  @PostMapping(value = "/rays/calculate-loan-amounts", produces = MediaType.APPLICATION_JSON_VALUE)
//  public ResponseEntity<String> calculateLoanAmounts(@RequestBody String reqBody) {
//    String response = raysLoanRequest.calculateLoanAmounts(reqBody);
//    return ResponseEntity.ok(response);
//  }
//
//  @PostMapping(value = "/rays/confirm", produces = MediaType.APPLICATION_JSON_VALUE)
//  public ResponseEntity<String> confirmRaysLoanRequest(@RequestBody String reqBody) {
//    JSONObject response = raysLoanConfirmation.pickAndProcess(reqBody);
//    return ResponseEntity.ok(response.toString());
//  }
//
//  @GetMapping(value = "/halalPay/markups", produces = MediaType.APPLICATION_JSON_VALUE)
//  public ResponseEntity<String> getHalalPayMarkups() {
//    String response = halalPayLoanRequest.getHilalPayMarkups();
//    return ResponseEntity.ok(response);
//  }
//
//  @PostMapping(value = "/halalPay/calculate-loan-amounts", produces = MediaType.APPLICATION_JSON_VALUE)
//  public ResponseEntity<String> calculateHalalPayLoanAmounts(@RequestBody String reqBody) {
//    String response = halalPayLoanRequest.calculateHilalPayLoanAmounts(reqBody);
//    return ResponseEntity.ok(response);
//  }
//
//  @PostMapping(value = "/halalPay/confirm", produces = MediaType.APPLICATION_JSON_VALUE)
//  public ResponseEntity<String> confirmHalalPayLoanRequest(@RequestBody String reqBody) {
//    JSONObject response = hilalPayLoanConfirmation.pickAndProcess(reqBody);
//    return ResponseEntity.ok(response.toString());
//  }
//
//  @PostMapping(value = "/halalPay/cancel-request", produces = MediaType.APPLICATION_JSON_VALUE)
//  public ResponseEntity<String> cancelHalalPayLoanRequest(@RequestBody String reqBody) {
//    JSONObject response = halalPayLoanCancel.pickAndProcess(reqBody);
//    return ResponseEntity.ok(response.toString());
//  }
//
//
//  @PostMapping(value = "/cbe-birr/min-app/call-back", produces = MediaType.APPLICATION_JSON_VALUE)
//  public ResponseEntity<String> postCBEBirrMinAppRes(
//      @RequestHeader(value = "authorization") String authorizationHeader,
//      @RequestBody String requestBody) {
//    log.info(requestBody);
//    JSONObject responseBody = new JSONObject();
//    try {
//      JSONObject requestObject = new JSONObject(requestBody);
//      responseBody = cbeBirrMiniAppPaymentFulfillment.pickAndProcess(authorizationHeader,
//          requestObject);
//
//      //min-app expect different response codes for different cases to try again
//      if (responseBody.getString("statusCode").equals("000")) {
//        return ResponseEntity.ok(responseBody.toString());
//      }
//
//      return ResponseEntity.badRequest().body(responseBody.toString());
//    } catch (Exception ex) {
//      responseBody.put("statusCode", "501").put("statusMessage", "Request failed");
//      log.error(ex.getMessage());
//      return ResponseEntity.internalServerError().body(responseBody.toString());
//    }
//  }
//
//  @PostMapping(value = "/airtime-purchase", produces = MediaType.APPLICATION_JSON_VALUE)
//  public ResponseEntity<String> postSahayAirtimePurchase(@RequestBody String requestBody) {
//    log.info(requestBody);
//    JSONObject responseBody = new JSONObject();
//    try {
//      sender.sendAirtimePurchase(requestBody.toString());
//    } catch (Exception ex) {
//      responseBody
//          .put("statusCode", ResponseCodes.SYSTEM_ERROR)
//          .put("statusDescription", "failed")
//          .put("statusMessage", "Request failed");
//      log.error(ex.getMessage());
//
//    }
//    return ResponseEntity.ok(responseBody.toString());
//  }
//
//  @PostMapping(value = "/amole/pay", produces = MediaType.APPLICATION_JSON_VALUE)
//  public ResponseEntity<String> amolePay(@RequestBody String requestBody) {
//    JSONObject responseBody = new JSONObject();
//    try {
//      JSONObject requestObject = new JSONObject(requestBody);
//      responseBody = amolePaymentFulfillment.pickAndProcess(requestObject);
//    } catch (Exception ex) {
//      responseBody
//          .put("statusCode", ResponseCodes.SYSTEM_ERROR)
//          .put("statusDescription", "failed controller")
//          .put("statusMessage", "Request failed");
//
//      log.error(ex.getMessage());
//    }
//    return ResponseEntity.ok(responseBody.toString());
//  }
//
//  @PostMapping(value = "/teleBirr/callBack", produces = MediaType.APPLICATION_JSON_VALUE)
//  public ResponseEntity<String> postTeleBirRes(@RequestBody String requestBody) {
//    log.info(requestBody);
//    JSONObject responseBody = new JSONObject();
//    try {
//
//      System.out.println("===============TEBEBIRR APP RESPONSE======================");
//      System.out.println();
//      System.out.println();
//      System.out.println(requestBody);
//      System.out.println();
//      System.out.println();
//      System.out.println("===============TEBEBIRR  APP RESPONSE======================");
//
//      responseBody = teleBirrPaymentFulfillment.pickAndProcess(requestBody);
//    } catch (Exception ex) {
//      responseBody.put("code", 5).put("msg", "Failed");
//      log.error(ex.getMessage());
//    }
//    return ResponseEntity.ok(responseBody.toString());
//  }
//
//  @PostMapping(value = "/mpesa/call-back", produces = MediaType.APPLICATION_JSON_VALUE)
//  public ResponseEntity<String> postMpesaRes(@RequestBody String requestBody) {
//    JSONObject responseBody = new JSONObject();
//    try {
//      log.info("\u001B[34m🟢 ========== MPESA RESPONSE START ==========\u001B[0m");
//      log.info("\u001B[36m📨 Incoming Request:\u001B[0m\n{}", requestBody);
//
//      // Simulate processing (replace this with your actual method)
//      responseBody = new JSONObject().put("ResultCode", "0").put("ResultDesc", "Success");
//
//      log.info("\u001B[32m✅ Successfully processed mpesa callback.\u001B[0m");
//      log.info("\u001B[34m🟢 ========== MPESA APP RESPONSE END ==========\u001B[0m");
//
//    } catch (Exception ex) {
//      responseBody = new JSONObject().put("ResultCode", "5").put("ResultDesc", "Failed");
//
//      log.error("\u001B[31m❌ Error while processing mpesa callback: {}\u001B[0m", ex.getMessage());
//    }
//
//    return ResponseEntity.ok(responseBody.toString());
//  }
//
////    @PostMapping(value = "/kasapay/call-back", produces = MediaType.APPLICATION_JSON_VALUE)
////    public ResponseEntity<String> kasapayWebhook(@RequestBody String requestBody) {
////        log.info("\u001B[34m🟢 ========== KASAPAY CALLBACK REQUEST START ==========\u001B[0m");
////        log.info("\u001B[36m📨 Incoming Request:\u001B[0m\n{}", requestBody);
////
////        JSONObject responseBody = kasaPayFulfillment.pickAndProcess(requestBody);
////
////        log.info("\u001B[34m🟢 ========== KASAPAY CALLBACK RESPONSE START ==========\u001B[0m");
////        log.info("\u001B[36m📨 Outgoing Response:\u001B[0m\n{}", responseBody);
////
////        return ResponseEntity.ok(responseBody.toString());
////    }
//
//
//  @PostMapping(value = "/tele-birr/min-app/call-back", produces = MediaType.APPLICATION_JSON_VALUE)
//  public ResponseEntity<String> x(@RequestBody String requestBody) {
//    log.info(requestBody);
//    JSONObject responseBody = new JSONObject();
//    try {
//      System.out.println("===============TEBEBIRR MINI APP RESPONSE======================");
//      System.out.println();
//      System.out.println();
//      System.out.println(requestBody);
//      System.out.println();
//      System.out.println();
//      System.out.println("===============TEBEBIRR MINI APP RESPONSE======================");
//      responseBody.put("code", 0).put("msg", "Success");
//    } catch (Exception ex) {
//      responseBody.put("code", 5).put("msg", "Failed");
//      log.error(ex.getMessage());
//    }
//    return ResponseEntity.ok(responseBody.toString());
//  }
//
//
//  @PostMapping(value = "/amole/fulfillment", produces = MediaType.APPLICATION_JSON_VALUE)
//  public ResponseEntity<String> amoleFulfillment(@RequestBody String requestBody) {
//    log.info(requestBody);
//    JSONObject responseBody = new JSONObject();
//    try {
//      JSONObject requestObject = new JSONObject(requestBody);
//      responseBody = amolePaymentFulfillment.pickAndProcess(requestObject);
//    } catch (Exception ex) {
//      responseBody
//          .put("statusCode", ResponseCodes.SYSTEM_ERROR)
//          .put("statusDescription", "failed")
//          .put("statusMessage", "Request failed");
//
//      log.error(ex.getMessage());
//    }
//    return ResponseEntity.ok(responseBody.toString());
//  }
//
//  @PostMapping(value = "/cbe-birr/call-back", produces = MediaType.APPLICATION_JSON_VALUE)
//  public ResponseEntity<String> postCBEBirrRes(@RequestBody String requestBody) {
//    log.info(requestBody);
//    JSONObject responseBody = new JSONObject();
//    try {
//
//      System.out.println("===============CBE BIRR RESPONSE======================");
//      System.out.println();
//      System.out.println();
//      System.out.println(requestBody);
//      System.out.println();
//      System.out.println();
//      System.out.println("===============CBE BIRR RESPONSE======================");
//
//      JSONObject requestObject = new JSONObject(requestBody);
//      responseBody = cbeBirrPaymentFulfillment.pickAndProcess(requestObject);
//    } catch (Exception ex) {
//      responseBody
//          .put("statusCode", ResponseCodes.SYSTEM_ERROR)
//          .put("statusDescription", "Failed")
//          .put("statusMessage", "Request failed");
//
//      log.error(ex.getMessage());
//    }
//
//    return ResponseEntity.ok(responseBody.toString());
//  }
//
//  @PostMapping(value = "/tele-birr/mini-app/call-back", produces = MediaType.APPLICATION_JSON_VALUE)
//  public ResponseEntity<String> teleBirrMiniApp(@RequestBody String requestBody) {
//    log.info(requestBody);
//    JSONObject responseBody = teleBirrMiniAppPaymentFulfillment.pickAndProcess(
//        new JSONObject(requestBody));
//    return ResponseEntity.ok(responseBody.toString());
//  }
//
//  @PostMapping(value = "/epg/call-back", produces = MediaType.APPLICATION_JSON_VALUE)
//  public ResponseEntity<String> postEPGRes(@RequestBody String requestBody) {
//    log.info(requestBody);
//    JSONObject response = epgPaymentFulfillment.pickAndProcess(requestBody);
//    return ResponseEntity.ok(response.toString());
//  }
//
//  @GetMapping(value = "/ipay/call-back", produces = MediaType.APPLICATION_JSON_VALUE)
//  public ResponseEntity<String> testCallBack(
//      @RequestParam String status, @RequestParam String id,
//      @RequestParam String mc, @RequestParam String txncd,
//      @RequestParam String channel
//  ) {
//
//    JSONObject response = iPayPaymentFulfillment.pickAndProcess(status, id, mc, txncd, channel);
//
//    // Perform the redirect
//    return ResponseEntity
//        .status(HttpStatus.FOUND)
//        .header(HttpHeaders.LOCATION, "https://commercepal.com/browse")
//        .body(response.toString());
//  }
//}
//
//
//
