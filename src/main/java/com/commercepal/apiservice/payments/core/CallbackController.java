package com.commercepal.apiservice.payments.core;

import com.commercepal.apiservice.payments.integration.sahay.SahayPayClient;
import com.commercepal.apiservice.utils.response.ResponseWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments/callback")
public class CallbackController {

    private final SahayPayClient sahayPayClient;
    private final com.commercepal.apiservice.payments.integration.pesapal.PesapalClient pesapalClient;

    @PostMapping(value = "/sahaypay", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseWrapper<String>> handleCallback(@RequestBody String requestJson) {
        JSONObject requestBody = new JSONObject(requestJson);
        sahayPayClient.handleSahayPayCallback(requestBody);
        return ResponseWrapper.success("Transaction completed successfully.");
    }

    @PostMapping(value = "/pesapal", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> handlePesapalCallback(@RequestBody String requestJson) {
        JSONObject requestBody = new JSONObject(requestJson);
        String response = pesapalClient.handlePesapalIpn(requestBody);
        return ResponseEntity.ok(response);
    }
}
