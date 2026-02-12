package com.commercepal.apiservice.settings.foreign_exchange.dto;

import com.commercepal.apiservice.shared.enums.SupportedCurrency;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;

/**
 * Response record for foreign exchange rate history information.
 * <p>
 * This record provides historical information about changes to foreign exchange rates, including
 * who made the change and when it occurred.
 */
@Builder
@Schema(
    name = "ForeignExchangeHistoryResponse",
    description = """
        Response payload containing historical information about foreign exchange rate changes.
        Tracks all modifications to exchange rates, including the previous rate values,
        when the change occurred, and who made the change.
        """
)
public record ForeignExchangeHistoryResponse(

    @Schema(
        description = "Unique identifier for the history record",
        example = "1"
    )
    Long id,

    @Schema(
        description = """
            The foreign exchange rate ID that this history record belongs to.
            References the ForeignExchange entity.
            """,
        example = "1"
    )
    Long foreignExchangeId,

    @Schema(
        description = """
            The base currency (source currency) at the time of this history record.
            This is the currency being converted from.
            """,
        example = "USD"
    )
    SupportedCurrency baseCurrency,

    @Schema(
        description = """
            The target currency (destination currency) at the time of this history record.
            This is the currency being converted to.
            """,
        example = "ETB"
    )
    SupportedCurrency targetCurrency,

    @Schema(
        description = """
            The exchange rate value at the time this history record was created.
            Represents how many units of the target currency equal one unit of the base currency.
            """,
        example = "180.000000"
    )
    BigDecimal rate,

    @Schema(
        description = """
            Timestamp when the exchange rate was changed.
            This represents the exact moment when the rate modification occurred.
            """,
        example = "2024-01-20T14:45:00"
    )
    LocalDateTime changedAt,

    @Schema(
        description = """
            Username or identifier of the user who made the change.
            This is typically the system username or user ID.
            """,
        example = "admin_user"
    )
    String changedBy,

    @Schema(
        description = """
            Full name of the user who made the change.
            This provides a human-readable identifier for who modified the exchange rate.
            """,
        example = "John Doe"
    )
    String changedByFullName
) {

}

