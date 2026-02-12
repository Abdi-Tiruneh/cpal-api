package com.commercepal.apiservice.payments.integration.pesapal;

import com.commercepal.apiservice.utils.HttpProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.Map;

import com.commercepal.apiservice.payments.oderPayment.OrderPayment;
import com.commercepal.apiservice.payments.oderPayment.dto.PaymentInitiationResponse;
import com.commercepal.apiservice.payments.oderPayment.enums.NextAction;
import com.commercepal.apiservice.users.customer.Customer;

import com.commercepal.apiservice.orders.enums.PaymentStatus;
import com.commercepal.apiservice.payments.core.ProcessSuccessPayment;
import com.commercepal.apiservice.payments.oderPayment.OrderPaymentRepository;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PesapalClient {

    private final PesapalConfig pesapalConfig;
    private final HttpProcessor httpProcessor;
    private final OrderPaymentRepository orderPaymentRepository;
    private final ProcessSuccessPayment processSuccessPayment;

    public String getAccessToken() {
        String url = pesapalConfig.getBaseUrl() + "/api/Auth/RequestToken";

        JSONObject requestBody = new JSONObject();
        requestBody.put("consumer_key", pesapalConfig.getConsumerKey());
        requestBody.put("consumer_secret", pesapalConfig.getConsumerSecret());

        Map<String, String> headers = Map.of(
                "Content-Type", "application/json",
                "Accept", "application/json");

        log.info("Requesting Pesapal access token | PesapalClient | getAccessToken | url={}", url);

        try {
            JSONObject response = httpProcessor.executeStructuredRequest(url, "POST", requestBody.toString(), headers)
                    .join();

            String statusCode = response.optString("StatusCode");
            String responseBodyStr = response.optString("ResponseBody");

            if ("200".equals(statusCode)) {
                JSONObject body = new JSONObject(responseBodyStr);
                if (body.has("token")) {
                    String token = body.getString("token");
                    log.info("Successfully retrieved Pesapal access token | PesapalClient | getAccessToken");
                    return token;
                } else {
                    log.error("Pesapal auth response missing token: {}", responseBodyStr);
                    throw new RuntimeException("Pesapal auth response missing token");
                }
            } else {
                log.error("Failed to get Pesapal access token. Status: {}, Response: {}", statusCode, responseBodyStr);
                throw new RuntimeException("Failed to get Pesapal access token: " + statusCode);
            }
        } catch (Exception e) {
            log.error("Error getting Pesapal access token | PesapalClient | getAccessToken", e);
            throw new RuntimeException("Error getting Pesapal access token", e);
        }
    }

    public PaymentInitiationResponse pickAndProcess(OrderPayment payment) {
        String currency = payment.getCurrency().name();
        if (!"KES".equals(currency) && !"USD".equals(currency)) {
            log.error("Unsupported currency | PesapalClient | pickAndProcess | currency={}", currency);
            throw new IllegalArgumentException("We currently only support KES and USD for Pesapal transactions.");
        }

        String token = getAccessToken();

        String url = pesapalConfig.getBaseUrl() + "/api/Transactions/SubmitOrderRequest";

        JSONObject billingAddress = new JSONObject();
        Customer customer = payment.getCustomer();

        String email = customer.getCredential().getEmailAddress();
        String phone = customer.getCredential().getPhoneNumber();
        String country = customer.getCountry();
        String firstName = customer.getFirstName();
        String lastName = customer.getLastName();
        String city = customer.getCity();
        String state = customer.getStateProvince();

        billingAddress.put("email_address", email != null ? email : "");
        billingAddress.put("phone_number", phone != null ? phone : "");

        if (country != null && country.length() >= 2) {
            billingAddress.put("country_code", country.substring(0, 2).toUpperCase());
        } else {
            billingAddress.put("country_code", "KE");
        }

        billingAddress.put("first_name", firstName != null ? firstName : "");
        billingAddress.put("last_name", lastName != null ? lastName : "");
        billingAddress.put("line_1", city != null && !city.isEmpty() ? city : "N/A");
        billingAddress.put("city", city != null ? city : "");

        if (state != null && state.length() > 3) {
            state = state.substring(0, 3);
        }
        billingAddress.put("state", state != null ? state : "");

        JSONObject requestBody = new JSONObject();
        requestBody.put("id", payment.getReference());
        requestBody.put("currency", payment.getCurrency().name());
        requestBody.put("amount", payment.getAmount());
        requestBody.put("description", "Order Payment Ref: " + payment.getReference());
        requestBody.put("callback_url", pesapalConfig.getCallbackUrl());
        requestBody.put("redirect_mode", "TOP_WINDOW");
        requestBody.put("notification_id", pesapalConfig.getIpn());
        requestBody.put("billing_address", billingAddress);

        Map<String, String> headers = Map.of(
                "Content-Type", "application/json",
                "Accept", "application/json",
                "Authorization", "Bearer " + token);

        log.info("Submitting Order Request to Pesapal | PesapalClient | pickAndProcess | url={}, reference={}", url,
                payment.getReference());

        try {
            payment.recordInitRequest(requestBody.toString());
            orderPaymentRepository.save(payment);

            JSONObject response = httpProcessor.executeStructuredRequest(url, "POST", requestBody.toString(), headers)
                    .join();

            payment.recordInitResponse(response.toString());
            orderPaymentRepository.save(payment);

            String statusCode = response.optString("StatusCode");
            String responseBodyStr = response.optString("ResponseBody");

            if ("200".equals(statusCode)) {
                JSONObject body = new JSONObject(responseBodyStr);

                String redirectUrl = body.optString("redirect_url");
                String orderTrackingId = body.optString("order_tracking_id");

                payment.setGatewayReference(orderTrackingId);
                payment.resolve(PaymentStatus.PENDING, "PENDING - Redirected to Pesapal");
                orderPaymentRepository.save(payment);

                return PaymentInitiationResponse.builder()
                        .success(true)
                        .paymentUrl(redirectUrl)
                        .paymentReference(payment.getReference())
                        .orderNumber(payment.getOrder().getOrderNumber())
                        .paymentProviderCode("PESAPAL")
                        .paymentInstructions("You will be redirected to Pesapal to complete your payment.")
                        .nextAction(NextAction.REDIRECT_TO_PAYMENT_URL)
                        .build();

            } else {
                String errorMessage = "Pesapal Submit Order Failed: " + statusCode + ", Body: " + responseBodyStr;
                log.error(errorMessage);
                payment.resolve(PaymentStatus.FAILED, errorMessage);
                orderPaymentRepository.save(payment);

                return PaymentInitiationResponse.builder()
                        .success(false)
                        .paymentReference(payment.getReference())
                        .orderNumber(payment.getOrder().getOrderNumber())
                        .paymentInstructions("Payment initiation failed at provider.")
                        .nextAction(NextAction.RETRY_PAYMENT)
                        .build();
            }

        } catch (Exception e) {
            log.error("Error in Pesapal pickAndProcess | PesapalClient | pickAndProcess | reference={}",
                    payment.getReference(), e);
            payment.resolve(PaymentStatus.FAILED, "Exception: " + e.getMessage());
            orderPaymentRepository.save(payment);
            throw new RuntimeException("Error processing Pesapal payment", e);
        }
    }

    public JSONObject getTransactionStatus(String orderTrackingId) {
        String token = getAccessToken();
        String url = pesapalConfig.getBaseUrl() + "/api/Transactions/GetTransactionStatus?orderTrackingId="
                + orderTrackingId;

        Map<String, String> headers = Map.of(
                "Accept", "application/json",
                "Content-Type", "application/json",
                "Authorization", "Bearer " + token);

        log.info("Checking Pesapal transaction status | PesapalClient | getTransactionStatus | trackingId={}",
                orderTrackingId);

        try {
            JSONObject response = httpProcessor.executeStructuredRequest(url, "GET", null, headers)
                    .join();

            String statusCode = response.optString("StatusCode");
            String responseBodyStr = response.optString("ResponseBody");

            if ("200".equals(statusCode)) {
                return new JSONObject(responseBodyStr);
            } else {
                log.error("Failed to get transaction status. Status: {}, Response: {}", statusCode, responseBodyStr);
                throw new RuntimeException("Failed to get transaction status: " + statusCode);
            }
        } catch (Exception e) {
            log.error("Error getting transaction status | PesapalClient | getTransactionStatus | trackingId={}",
                    orderTrackingId, e);
            throw new RuntimeException("Error getting transaction status", e);
        }
    }

    public String handlePesapalIpn(JSONObject ipnData) {
        log.info("Received Pesapal IPN callback | PesapalClient | handlePesapalIpn | data={}", ipnData);

        String notificationType = ipnData.optString("OrderNotificationType");
        String trackingId = ipnData.optString("OrderTrackingId");
        String merchantReference = ipnData.optString("OrderMerchantReference");

        if ("IPNCHANGE".equals(notificationType)) {
            log.info("Processing IPN change | PesapalClient | handlePesapalIpn | reference={}, trackingId={}",
                    merchantReference, trackingId);

            try {
                // 1. Find the payment record
                Optional<OrderPayment> paymentOpt = orderPaymentRepository.findByReference(merchantReference);
                if (paymentOpt.isEmpty()) {
                    log.error("OrderPayment not found | PesapalClient | handlePesapalIpn | reference={}",
                            merchantReference);
                    return new JSONObject()
                            .put("orderNotificationType", notificationType)
                            .put("orderTrackingId", trackingId)
                            .put("orderMerchantReference", merchantReference)
                            .put("status", 200)
                            .toString();
                }
                OrderPayment payment = paymentOpt.get();

                // 2. Check overlap/idempotency
                if (payment.getStatus() == PaymentStatus.SUCCESS) {
                    log.info("Payment already marked as SUCCESS | PesapalClient | handlePesapalIpn | reference={}",
                            merchantReference);
                    return new JSONObject()
                            .put("orderNotificationType", notificationType)
                            .put("orderTrackingId", trackingId)
                            .put("orderMerchantReference", merchantReference)
                            .put("status", 200)
                            .toString();
                }

                // 3. Get latest status from Pesapal
                JSONObject transactionStatus = getTransactionStatus(trackingId);
                log.info("Transaction Status Response | PesapalClient | handlePesapalIpn | response={}",
                        transactionStatus);

                String paymentStatusDescription = transactionStatus.optString("payment_status_description");
                String statusCode = transactionStatus.optString("status_code"); // 1 - COMPLETED, 2 - FAILED, etc.

                // Record verification log
                payment.recordVerification(trackingId, transactionStatus.toString());
                payment.recordWebhook(ipnData.toString(), paymentStatusDescription);
                orderPaymentRepository.save(payment);

                // 4. Update local status
                if ("COMPLETED".equalsIgnoreCase(paymentStatusDescription) || "1".equals(statusCode)) {
                    log.info("Payment SUCCESS | PesapalClient | handlePesapalIpn | reference={}", merchantReference);
                    payment.resolve(PaymentStatus.SUCCESS, "Pesapal Payment Completed");
                    orderPaymentRepository.save(payment);

                    // Trigger downstream fulfillment
                    try {
                        processSuccessPayment.pickAndProcess(payment);
                    } catch (Exception e) {
                        log.error("Error in downstream fulfillment | PesapalClient | handlePesapalIpn | reference={}",
                                merchantReference, e);
                    }

                } else if ("FAILED".equalsIgnoreCase(paymentStatusDescription) || "2".equals(statusCode)) {
                    log.warn("Payment FAILED | PesapalClient | handlePesapalIpn | reference={}", merchantReference);
                    payment.resolve(PaymentStatus.FAILED, "Pesapal Payment Failed: " + paymentStatusDescription);
                    orderPaymentRepository.save(payment);
                } else if ("INVALID".equalsIgnoreCase(paymentStatusDescription) || "0".equals(statusCode)) {
                    log.warn("Payment INVALID | PesapalClient | handlePesapalIpn | reference={}", merchantReference);
                    payment.resolve(PaymentStatus.FAILED, "Pesapal Payment Invalid");
                    orderPaymentRepository.save(payment);
                } else if ("REVERSED".equalsIgnoreCase(paymentStatusDescription) || "3".equals(statusCode)) {
                    log.warn("Payment REVERSED | PesapalClient | handlePesapalIpn | reference={}", merchantReference);
                    payment.resolve(PaymentStatus.FAILED, "Pesapal Payment Reversed");
                    orderPaymentRepository.save(payment);
                } else {
                    log.info(
                            "Payment status pending/unknown | PesapalClient | handlePesapalIpn | status={}, reference={}",
                            paymentStatusDescription, merchantReference);
                }

            } catch (Exception e) {
                log.error("Error processing IPN | PesapalClient | handlePesapalIpn | trackingId={}", trackingId, e);
                return new JSONObject()
                        .put("orderNotificationType", notificationType)
                        .put("orderTrackingId", trackingId)
                        .put("orderMerchantReference", merchantReference)
                        .put("status", 500)
                        .toString();
            }

            return new JSONObject()
                    .put("orderNotificationType", notificationType)
                    .put("orderTrackingId", trackingId)
                    .put("orderMerchantReference", merchantReference)
                    .put("status", 200)
                    .toString();

        } else {
            log.warn("Received unknown Pesapal notification type | PesapalClient | handlePesapalIpn | type={}",
                    notificationType);
            return new JSONObject()
                    .put("orderNotificationType", notificationType)
                    .put("orderTrackingId", trackingId)
                    .put("orderMerchantReference", merchantReference)
                    .put("status", 500)
                    .toString();
        }
    }
}
