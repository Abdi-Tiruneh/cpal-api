package com.commercepal.apiservice.orders.core.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * DTO representing delivery address information.
 * Contains essential address details for order delivery.
 */
@Builder
@Schema(name = "DeliveryAddressDto", description = "Delivery address information for order shipment")
public record DeliveryAddressDto(
    @Schema(description = "Full name of the recipient", example = "John Doe", requiredMode = Schema.RequiredMode.REQUIRED)
    String fullName,

    @Schema(description = "Contact phone number", example = "+251911234567", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String phone,

    @Schema(description = "City for delivery", example = "Addis Ababa", requiredMode = Schema.RequiredMode.REQUIRED)
    String city,

    @Schema(description = "Full formatted address string containing complete address information", example = "Rwanda Street - Main Street in Bole - Addis Ababa,Addis Ababa,Addis Ababa,Ethiopia 1000 A**********h +251 91****901", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String fullAddress
) {
}
