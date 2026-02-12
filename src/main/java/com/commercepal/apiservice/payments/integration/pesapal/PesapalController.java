package com.commercepal.apiservice.payments.integration.pesapal;

import com.commercepal.apiservice.utils.CurrentUserService;
import com.commercepal.apiservice.utils.HttpProcessor;
import com.commercepal.apiservice.users.role.RoleCode;
import io.swagger.v3.oas.annotations.Hidden;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/pesapal/register-ipn")
public class PesapalController {

    private final PesapalConfig pesapalConfig;
    private final HttpProcessor httpProcessor;
    private final PesapalClient pesapalClient;
    private final CurrentUserService currentUserService;

    @GetMapping
    public String registerIpn() {
        currentUserService.ensureHasAnyRole(RoleCode.ROLE_SUPER_ADMIN);
        String url = pesapalConfig.getBaseUrl() + "/api/URLSetup/RegisterIPN";

        JSONObject requestBody = new JSONObject();
        requestBody.put("url", pesapalConfig.getIpnUrl());
        requestBody.put("ipn_notification_type", "POST");

        Map<String, String> headers = Map.of(
                "Content-Type", "application/json",
                "Accept", "application/json",
                "Authorization", "Bearer " + pesapalClient.getAccessToken());

        log.info("Registering Pesapal IPN URL | PesapalClient | registerIpn | url={}", pesapalConfig.getIpnUrl());

        try {
            JSONObject response = httpProcessor.executeStructuredRequest(url, "POST", requestBody.toString(), headers)
                    .join();
            String statusCode = response.optString("StatusCode");
            String responseBodyStr = response.optString("ResponseBody");

            if ("200".equals(statusCode)) {
                JSONObject body = new JSONObject(responseBodyStr);
                if (body.has("ipn_id")) {
                    String ipnId = body.getString("ipn_id");
                    log.info("Successfully registered Pesapal IPN | PesapalClient | registerIpn | id={}", ipnId);
                    return ipnId;
                } else {
                    log.error("Pesapal IPN registration response missing ipn_id: {}", responseBodyStr);
                    throw new RuntimeException("Pesapal IPN registration response missing ipn_id");
                }
            } else {
                log.error("Failed to register Pesapal IPN. Status: {}, Response: {}", statusCode, responseBodyStr);
                throw new RuntimeException("Failed to register Pesapal IPN: " + statusCode);
            }
        } catch (Exception e) {
            log.error("Error registering Pesapal IPN | PesapalClient | registerIpn", e);
            throw new RuntimeException("Error registering Pesapal IPN", e);
        }
    }

}
