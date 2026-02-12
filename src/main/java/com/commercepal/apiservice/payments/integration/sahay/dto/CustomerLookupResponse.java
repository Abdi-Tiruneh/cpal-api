package com.commercepal.apiservice.payments.integration.sahay.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * Response DTO for SahayPay customer account lookup.
 */
@Builder
@Schema(name = "CustomerLookupResponse", description = "Response containing verified customer account details from SahayPay")
public record CustomerLookupResponse(

    @Schema(description = "Verified customer name from SahayPay", example = "ABDI MOHAMED", requiredMode = Schema.RequiredMode.REQUIRED)
    String customerName
) {
}
