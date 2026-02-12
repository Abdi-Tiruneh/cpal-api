package com.commercepal.apiservice.settings.foreign_exchange.dto;

import com.commercepal.apiservice.shared.enums.SupportedCurrency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Builder;

/**
 * Request record for creating or updating foreign exchange rates.
 * <p>
 * This record is used to set exchange rates between two currencies. The base currency represents
 * the source currency, and the target currency represents the destination currency. The rate
 * indicates how many units of the target currency equal one unit of the base currency.
 */
@Builder
@Schema(
    name = "ForeignExchangeRequest",
    description = """
        Request payload for creating or updating foreign exchange rates.
        Used to set the exchange rate between a base currency and a target currency.
        The rate represents how many units of the target currency equal one unit of the base currency.
        """
)
public record ForeignExchangeRequest(

    @Schema(
        description = """
            The base currency (source currency) for the exchange rate.
            This is the currency being converted from.
            Must be a supported currency (e.g., USD).
            """,
        example = "USD"
    )
    @NotNull(message = "Base currency is required")
    SupportedCurrency baseCurrency,

    @Schema(
        description = """
            The target currency (destination currency) for the exchange rate.
            This is the currency being converted to.
            Must be a supported currency (e.g., ETB).
            """,
        example = "ETB"
    )
    @NotNull(message = "Target currency is required")
    SupportedCurrency targetCurrency,

    @Schema(
        description = """
            The exchange rate value.
            Represents how many units of the target currency equal one unit of the base currency.
            Must be a positive decimal number with up to 8 decimal places for precision.
            Minimum value: 0.00000001
            """,
        example = "180.000000"
    )
    @NotNull(message = "Rate is required")
    @DecimalMin(value = "0.00000001", inclusive = true, message = "Rate must be greater than zero")
    BigDecimal rate
) {

}

