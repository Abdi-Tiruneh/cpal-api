package com.commercepal.apiservice.promotions.affiliate.withdrawal.dto;

import com.commercepal.apiservice.promotions.affiliate.withdrawal.AffiliateWithdrawal;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WithdrawalRequestDto {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @DecimalMax(value = "999999.9999", message = "Amount exceeds maximum limit")
    @Digits(integer = 6, fraction = 4, message = "Amount must have at most 6 integer digits and 4 decimal places")
    private BigDecimal amount;

    @NotNull(message = "Payment method is required")
    private AffiliateWithdrawal.PaymentMethod paymentMethod;

    @NotBlank(message = "Account number is required")
    @Size(max = 100, message = "Account number must not exceed 100 characters")
    private String accountNumber;

    @Size(max = 100, message = "Bank name must not exceed 100 characters")
    private String bankName;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}
