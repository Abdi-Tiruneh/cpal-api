package com.commercepal.apiservice.users.customer.address.dto;

import com.commercepal.apiservice.users.customer.address.AddressSourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

/**
 * AddressResponse
 * <p>
 * Read model for returning customer addresses to clients with audit metadata.
 */
@Builder
@Jacksonized
public record AddressResponse(
    @Schema(description = "Address identifier", example = "98765") Long id,

    @Schema(description = "Indicates if this is the customer's default address", example = "true") boolean isDefault,

    @Schema(description = "Whether address can be edited (false if active orders exist)", example = "true") boolean canEdit,

    @Schema(description = "Whether address can be deleted (false if active orders exist)", example = "false") boolean canDelete,

    @Schema(description = "Recipient name", example = "Amina Yusuf") String receiverName,

    @Schema(description = "Subscriber phone number without country code", example = "501234567") String phoneNumber,

    @Schema(description = "Country of the address", example = "AE") String country,

    @Schema(description = "State / Emirate / Region", example = "Dubai") String state,

    @Schema(description = "City name", example = "Dubai") String city,

    @Schema(description = "District / Ward / Woreda", example = "Al Rigga") String district,

    @Schema(description = "Street / area / road", example = "Al Rigga Rd") String street,

    @Schema(description = "House or building number", example = "Bldg 14") String houseNumber,

    @Schema(description = "Nearby landmark", example = "Near Al Ghurair Centre") String landmark,

    @Schema(description = "Address line 1", example = "Apartment 1203") String addressLine1,

    @Schema(description = "Address line 2", example = "Tower B") String addressLine2,

    @Schema(description = "GPS latitude (decimal degrees)", example = "25.2663") String latitude,

    @Schema(description = "GPS longitude (decimal degrees)", example = "55.3161") String longitude,

    @Schema(description = "Source of address capture", allowableValues = {
        "MANUAL",
        "GOOGLE_MAPS",
        "MAP_PIN",
        "DELIVERY_AGENT",
        "PREVIOUS_ADDRESS",
        "VERIFIED_ID",
        "GPS_LOCATION",
        "EXTERNAL_PROVIDER"
    }, example = "GOOGLE_MAPS") AddressSourceType addressSource,

    @Schema(description = "Audit timestamp - created", example = "2025-01-02T10:15:30Z") LocalDateTime createdAt,

    @Schema(description = "Audit timestamp - last updated", example = "2025-01-10T08:45:10Z") LocalDateTime updatedAt) {

}
