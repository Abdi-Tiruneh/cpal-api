package com.commercepal.apiservice.users.customer.dto;

import com.commercepal.apiservice.shared.enums.Channel;
import com.commercepal.apiservice.shared.enums.SupportedCurrency;
import com.commercepal.apiservice.users.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;

/**
 * Simple customer detail response DTO for admin table view.
 * <p>
 * This record provides essential customer information in a flat structure, combining key fields
 * from Customer and Credential entities. Optimized for table/list views in admin
 * interfaces.
 */
@Builder
@Schema(
    name = "CustomerDetailResponse",
    description = """
        Simple customer detail response for admin table view.
        Contains essential customer information in a flat structure,
        combining key fields from Customer and AccountCredential entities.
        """
)
public record CustomerDetailResponse(

    @Schema(
        description = "Unique identifier for the customer record",
        example = "1"
    )
    Long id,

    @Schema(
        description = "Unique account number assigned to the customer",
        example = "CUS2024000001"
    )
    String accountNumber,

    @Schema(
        description = "Customer's full name (first name + last name)",
        example = "John Doe"
    )
    String fullName,

    @Schema(
        description = "Email address associated with the customer account",
        example = "john.doe@example.com"
    )
    String emailAddress,

    @Schema(
        description = "Phone number associated with the customer account",
        example = "+251912345678"
    )
    String phoneNumber,

    @Schema(
        description = "Country of the customer (ISO 3166-1 alpha-2 code)",
        example = "ET"
    )
    String country,

    @Schema(
        description = "City where the customer is located",
        example = "Addis Ababa"
    )
    String city,

    @Schema(
        description = "Customer's preferred currency for transactions",
        example = "ETB"
    )
    SupportedCurrency preferredCurrency,

    @Schema(
        description = "Account status of the customer",
        example = "ACTIVE"
    )
    UserStatus status,

    @Schema(
        description = "Whether the email is verified",
        example = "true"
    )
    Boolean isEmailVerified,

    @Schema(
        description = "Whether the phone is verified",
        example = "true"
    )
    Boolean isPhoneVerified,

    @Schema(
        description = "Channel through which the customer registered",
        example = "WEB"
    )
    Channel registrationChannel,

    @Schema(
        description = "Timestamp when the customer account was created",
        example = "2024-01-15T10:30:00"
    )
    LocalDateTime createdAt,

    @Schema(
        description = "Timestamp when the customer last signed in",
        example = "2024-01-20T14:45:00"
    )
    LocalDateTime lastSignedInAt

) {

}
