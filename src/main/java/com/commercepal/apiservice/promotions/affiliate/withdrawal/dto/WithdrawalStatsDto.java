package com.commercepal.apiservice.promotions.affiliate.withdrawal.dto;

import com.commercepal.apiservice.shared.enums.SupportedCurrency;
import java.math.RoundingMode;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WithdrawalStatsDto {

    private long totalWithdrawals;
    private long pendingWithdrawals;
    private long approvedWithdrawals;
    private long rejectedWithdrawals;
    private long paidWithdrawals;

    private BigDecimal totalAmount;
    private BigDecimal pendingAmount;
    private BigDecimal approvedAmount;
    private BigDecimal rejectedAmount;
    private BigDecimal paidAmount;
    private SupportedCurrency currency;

    public WithdrawalStatsDto() {
        this.totalWithdrawals = 0;
        this.pendingWithdrawals = 0;
        this.approvedWithdrawals = 0;
        this.rejectedWithdrawals = 0;
        this.paidWithdrawals = 0;
        this.totalAmount = BigDecimal.ZERO;
        this.pendingAmount = BigDecimal.ZERO;
        this.approvedAmount = BigDecimal.ZERO;
        this.rejectedAmount = BigDecimal.ZERO;
        this.paidAmount = BigDecimal.ZERO;
        this.currency = SupportedCurrency.ETB;
    }

    public void normalizeScale() {
        this.totalAmount = format(this.totalAmount);
        this.pendingAmount = format(this.pendingAmount);
        this.approvedAmount = format(this.approvedAmount);
        this.rejectedAmount = format(this.rejectedAmount);
        this.paidAmount = format(this.paidAmount);
    }


    public BigDecimal format(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

}
