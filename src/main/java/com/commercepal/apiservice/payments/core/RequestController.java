package com.commercepal.apiservice.payments.core;

import com.commercepal.apiservice.payments.integration.sahay.SahayPayClient;
import com.commercepal.apiservice.payments.integration.sahay.dto.CustomerLookupResponse;
import com.commercepal.apiservice.utils.response.ResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(
    name = "SahayPay Integration",
    description = "APIs for SahayPay payment gateway integration including customer lookup operations"
)
public class RequestController {

  private final SahayPayClient sahayPayClient;

  @GetMapping(value = "/sahaypay/customer-lookup", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      summary = "Lookup customer account"
  )
  public ResponseEntity<ResponseWrapper<CustomerLookupResponse>> lookupCustomer(
      @Parameter(
          description = "Phone number in format: 251 followed by 9 digits (e.g., 251912345678)",
          required = true,
          example = "251912345678"
      )
      @RequestParam String phoneNumber
  ) {
    log.info("[SAHAYPAY-API] Customer lookup request received - phoneNumber: {}",
        PhoneValidationUtil.maskPhoneNumber(phoneNumber));

    String normalizedPhone = PhoneValidationUtil.normalizeWithoutPlus(phoneNumber);

    CustomerLookupResponse response = sahayPayClient.lookupCustomerAccount(normalizedPhone);
    log.info("[SAHAYPAY-API] Customer lookup successful - phoneNumber: {}",
        PhoneValidationUtil.maskPhoneNumber(phoneNumber));
    return ResponseWrapper.success("Customer lookup completed successfully", response);
  }
}
