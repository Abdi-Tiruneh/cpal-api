package com.commercepal.apiservice.users.customer.address.dto;

import com.commercepal.apiservice.users.customer.address.AddressSourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

/**
 * AddressRequest
 * <p>
 * Record-based DTO for ingesting address data with a fluent builder that works cleanly with
 * Jackson. Designed for international addresses while maintaining normalized field names.
 */
@Builder
@Jacksonized
public record AddressRequest(
    @Schema(description = "Full recipient name for delivery label", example = "Amina Yusuf") @NotBlank @Size(max = 120) String receiverName,

    @Schema(description = "Subscriber phone number without country code", example = "+251911223344") @NotBlank @Size(max = 32) String phoneNumber,

    @Schema(description = "ISO alpha-2 or business-supported country enum", example = "AE", allowableValues = {
        "ET", "KE", "AE", "SO"}) @NotNull String countryCode,

    @Schema(description = "State/Emirate/Region (first-level admin)", example = "Dubai") @Size(max = 120) String state,

    @Schema(description = "City name", example = "Dubai") @Size(max = 120) String city,

    @Schema(description = "District/Woreda/Ward (third-level)", example = "Al Rigga") @Size(max = 120) String district,

    @Schema(description = "Street / area / road", example = "Al Rigga Rd") @Size(max = 255) String street,

    @Schema(description = "House / building / villa number", example = "Bldg 14") @Size(max = 60) String houseNumber,

    @Schema(description = "Landmark to help couriers locate address", example = "Near Al Ghurair Centre") @Size(max = 255) String landmark,

    @Schema(description = "Additional address line 1", example = "Apartment 1203") @Size(max = 255) String addressLine1,

    @Schema(description = "Additional address line 2", example = "Tower B") @Size(max = 255) String addressLine2,

    @Schema(description = "GPS latitude in decimal degrees", example = "25.2663") @Size(max = 50) String latitude,

    @Schema(description = "GPS longitude in decimal degrees", example = "55.3161") @Size(max = 50) String longitude,

    @Schema(description = "Source of address capture", allowableValues = {
        "MANUAL",
        "GOOGLE_MAPS",
        "MAP_PIN",
        "DELIVERY_AGENT",
        "PREVIOUS_ADDRESS",
        "VERIFIED_ID",
        "GPS_LOCATION",
        "EXTERNAL_PROVIDER"
    }, example = "GOOGLE_MAPS") @NotNull AddressSourceType addressSource,

    @Schema(description = "Flag to mark this as the default shipping address", example = "true") Boolean isDefault) {

}
