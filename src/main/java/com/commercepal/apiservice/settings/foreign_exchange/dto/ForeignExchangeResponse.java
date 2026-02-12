package com.commercepal.apiservice.settings.foreign_exchange.dto;

import com.commercepal.apiservice.shared.enums.SupportedCurrency;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;

/**
 * Response record for foreign exchange rate information.
 * <p>
 * This record provides complete information about a foreign exchange rate, including audit fields
 * for tracking creation and modification timestamps.
 */
@Builder
@Schema(
    name = "ForeignExchangeResponse",
    description = """
        Response payload containing foreign exchange rate information.
        Includes the exchange rate between base and target currencies,
        along with audit information such as creation and update timestamps.
        """
)
public record ForeignExchangeResponse(

    @Schema(
        description = "Unique identifier for the foreign exchange rate record",
        example = "1"
    )
    Long id,

    @Schema(
        description = """
            The base currency (source currency) for the exchange rate.
            This is the currency being converted from.
            """,
        example = "USD"
    )
    SupportedCurrency baseCurrency,

    @Schema(
        description = """
            The target currency (destination currency) for the exchange rate.
            This is the currency being converted to.
            """,
        example = "ETB"
    )
    SupportedCurrency targetCurrency,

    @Schema(
        description = """
            The exchange rate value.
            Represents how many units of the target currency equal one unit of the base currency.
            Stored with precision up to 6 decimal places.
            """,
        example = "180.000000"
    )
    BigDecimal rate,

    @Schema(
        description = "Timestamp when the foreign exchange rate was first created",
        example = "2024-01-15T10:30:00"
    )
    LocalDateTime createdAt,

    @Schema(
        description = "Timestamp when the foreign exchange rate was last updated",
        example = "2024-01-20T14:45:00"
    )
    LocalDateTime updatedAt
) {

}

