package com.commercepal.apiservice.promotions.affiliate.withdrawal.validation;

import com.commercepal.apiservice.shared.exceptions.business.BadRequestException;
import com.commercepal.apiservice.promotions.affiliate.commission.AffiliateCommissionService;
import com.commercepal.apiservice.promotions.affiliate.withdrawal.AffiliateWithdrawal;
import com.commercepal.apiservice.promotions.affiliate.withdrawal.AffiliateWithdrawal.PaymentMethod;
import com.commercepal.apiservice.promotions.affiliate.withdrawal.AffiliateWithdrawalRepository;
import com.commercepal.apiservice.promotions.affiliate.withdrawal.dto.WithdrawalRequestDto;
import com.commercepal.apiservice.promotions.affiliate.user.Affiliate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.regex.Pattern;

@Component
@Slf4j
@RequiredArgsConstructor
public class WithdrawalValidator {

    private final AffiliateCommissionService commissionService;
    private final AffiliateWithdrawalRepository withdrawalRepository;

    private static final BigDecimal MIN_WITHDRAWAL_AMOUNT = new BigDecimal("10.00");
    private static final BigDecimal MAX_WITHDRAWAL_AMOUNT = new BigDecimal("10000.00");

    // Regex patterns for validation
    private static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern.compile(
        "^[A-Za-z0-9\\-\\s]{1,100}$");
    private static final Pattern BANK_NAME_PATTERN = Pattern.compile("^[A-Za-z\\s]{1,100}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[0-9\\-\\s()]{7,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /**
     * Validate withdrawal request
     */
    public void validateWithdrawalRequest(WithdrawalRequestDto requestDto, Affiliate affiliate) {
        validateAmount(requestDto.getAmount());
        validatePaymentMethod(requestDto.getPaymentMethod());
        validateAccountNumber(requestDto.getAccountNumber(), requestDto.getPaymentMethod());
        validateBankName(requestDto.getBankName(), requestDto.getPaymentMethod());
        validateNotes(requestDto.getNotes());
        validateAffiliateEligibility(affiliate);
        validateAvailableBalance(requestDto.getAmount(), affiliate);
    }

    /**
     * Validate withdrawal amount
     */
    private void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new BadRequestException("Withdrawal amount is required");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Withdrawal amount must be greater than zero");
        }

        if (amount.compareTo(MIN_WITHDRAWAL_AMOUNT) < 0) {
            throw new BadRequestException(
                "Minimum withdrawal amount is ETB " + MIN_WITHDRAWAL_AMOUNT);
        }

        if (amount.compareTo(MAX_WITHDRAWAL_AMOUNT) > 0) {
            throw new BadRequestException(
                "Maximum withdrawal amount is ETB " + MAX_WITHDRAWAL_AMOUNT);
        }

        // Check decimal places
        if (amount.scale() > 4) {
            throw new BadRequestException("Amount cannot have more than 4 decimal places");
        }
    }

    /**
     * Validate payment method
     */
    private void validatePaymentMethod(PaymentMethod paymentMethod) {
        if (paymentMethod == null) {
            throw new BadRequestException("Payment method is required");
        }

        // Add specific validations for each payment method if needed
        switch (paymentMethod) {
            case BANK:
            case TELEBIRR:
            case EBIRR:
            case OTHER:
                break;
            default:
                throw new BadRequestException("Invalid payment method: " + paymentMethod);
        }
    }

    /**
     * Validate account number based on payment method
     */
    private void validateAccountNumber(String accountNumber, PaymentMethod paymentMethod) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new BadRequestException("Account number is required");
        }

        if (accountNumber.length() > 100) {
            throw new BadRequestException("Account number must not exceed 100 characters");
        }

        // Basic format validation
        if (!ACCOUNT_NUMBER_PATTERN.matcher(accountNumber).matches()) {
            throw new BadRequestException("Invalid account number format");
        }

        // Payment method specific validations
        switch (paymentMethod) {
            case BANK:
                validateBankAccountNumber(accountNumber);
                break;
            case TELEBIRR:
                validateTeleBirrAccountNumber(accountNumber);
                break;
            case EBIRR:
                validateAmoleAccountNumber(accountNumber);
                break;
            case OTHER:
                // Less strict validation for other payment methods
                break;
        }
    }

    /**
     * Validate bank name
     */
    private void validateBankName(String bankName, PaymentMethod paymentMethod) {
        if (paymentMethod == AffiliateWithdrawal.PaymentMethod.BANK) {
            if (bankName == null || bankName.trim().isEmpty()) {
                throw new BadRequestException("Bank name is required for bank transfers");
            }

            if (bankName.length() > 100) {
                throw new BadRequestException("Bank name must not exceed 100 characters");
            }

            if (!BANK_NAME_PATTERN.matcher(bankName).matches()) {
                throw new BadRequestException("Invalid bank name format");
            }
        }
    }

    /**
     * Validate notes
     */
    private void validateNotes(String notes) {
        if (notes != null && notes.length() > 500) {
            throw new BadRequestException("Notes must not exceed 500 characters");
        }
    }

    /**
     * Validate affiliate eligibility
     */
    private void validateAffiliateEligibility(Affiliate affiliate) {
        if (affiliate.getIsActive() == null || !affiliate.getIsActive()) {
            throw new BadRequestException("Affiliate account is not active");
        }

        // Add more eligibility checks as needed
        // e.g., minimum balance, account verification status, etc.
    }

    /**
     * Validate that withdrawal amount does not exceed available balance.
     * Available balance = Current balance (unpaid commissions) - Pending withdrawals - Approved withdrawals
     */
    private void validateAvailableBalance(BigDecimal withdrawalAmount, Affiliate affiliate) {
        log.debug("Validating available balance | WithdrawalValidator | validateAvailableBalance | affiliateId={}", affiliate.getId());

        // Get current balance (unpaid commissions)
        BigDecimal currentBalance = commissionService.calculateCurrentBalance(affiliate.getId());

        // Get pending withdrawals amount (COALESCE ensures it never returns null)
        BigDecimal pendingWithdrawals = withdrawalRepository.getTotalAmountByAffiliateAndStatus(
            affiliate, AffiliateWithdrawal.WithdrawalStatus.PENDING);

        // Get approved withdrawals amount (already committed but not yet paid)
        BigDecimal approvedWithdrawals = withdrawalRepository.getTotalAmountByAffiliateAndStatus(
            affiliate, AffiliateWithdrawal.WithdrawalStatus.APPROVED);

        // Calculate available balance
        BigDecimal availableBalance = currentBalance
            .subtract(pendingWithdrawals)
            .subtract(approvedWithdrawals);

        // Ensure available balance is not negative
        if (availableBalance.compareTo(BigDecimal.ZERO) < 0) {
            availableBalance = BigDecimal.ZERO;
        }

        log.debug("Balance breakdown | WithdrawalValidator | validateAvailableBalance | currentBalance={}, pendingWithdrawals={}, approvedWithdrawals={}, availableBalance={}",
            currentBalance, pendingWithdrawals, approvedWithdrawals, availableBalance);

        // Validate withdrawal amount against available balance
        if (withdrawalAmount.compareTo(availableBalance) > 0) {
            log.warn("Withdrawal amount exceeds available balance | WithdrawalValidator | validateAvailableBalance | withdrawalAmount={}, availableBalance={}, affiliateId={}",
                withdrawalAmount, availableBalance, affiliate.getId());
            throw new BadRequestException(
                String.format("Insufficient balance. Available balance: ETB %s, Requested amount: ETB %s",
                    availableBalance.setScale(2, java.math.RoundingMode.HALF_UP),
                    withdrawalAmount.setScale(2, java.math.RoundingMode.HALF_UP)));
        }

        log.info("Balance validation passed | WithdrawalValidator | validateAvailableBalance | affiliateId={}, availableBalance={}, requestedAmount={}",
            affiliate.getId(), availableBalance, withdrawalAmount);
    }

    /**
     * Validate bank account number
     */
    private void validateBankAccountNumber(String accountNumber) {
        // Basic bank account validation
        if (accountNumber.length() < 8 || accountNumber.length() > 20) {
            throw new BadRequestException(
                "Bank account number must be between 8 and 20 characters");
        }
    }

    /**
     * Validate TeleBirr account number
     */
    private void validateTeleBirrAccountNumber(String accountNumber) {
        // TeleBirr phone number validation
        if (!PHONE_PATTERN.matcher(accountNumber).matches()) {
            throw new BadRequestException("Invalid TeleBirr phone number format");
        }
    }

    /**
     * Validate Amole account number
     */
    private void validateAmoleAccountNumber(String accountNumber) {
        // Amole account validation
        if (accountNumber.length() < 10 || accountNumber.length() > 15) {
            throw new BadRequestException(
                "Amole account number must be between 10 and 15 characters");
        }
    }

    /**
     * Validate status transition
     */
    public void validateStatusTransition(AffiliateWithdrawal.WithdrawalStatus currentStatus,
                                         AffiliateWithdrawal.WithdrawalStatus newStatus) {
        if (currentStatus == null || newStatus == null) {
            throw new BadRequestException("Status cannot be null");
        }

        if (currentStatus == newStatus) {
            throw new BadRequestException("Status is already " + currentStatus);
        }

        boolean isValidTransition = switch (currentStatus) {
            case PENDING -> newStatus == AffiliateWithdrawal.WithdrawalStatus.APPROVED ||
                newStatus == AffiliateWithdrawal.WithdrawalStatus.REJECTED;
            case APPROVED -> newStatus == AffiliateWithdrawal.WithdrawalStatus.PAID ||
                newStatus == AffiliateWithdrawal.WithdrawalStatus.REJECTED;
            case REJECTED, PAID -> false; // Terminal states
        };

        if (!isValidTransition) {
            throw new BadRequestException(
                "Invalid status transition from " + currentStatus + " to " + newStatus);
        }
    }

    /**
     * Validate search parameters
     */
    public void validateSearchParameters(String sortBy, String sortDirection) {
        if (sortBy != null && !isValidSortField(sortBy)) {
            throw new BadRequestException("Invalid sort field: " + sortBy);
        }

        if (sortDirection != null && !sortDirection.matches("(?i)(ASC|DESC)")) {
            throw new BadRequestException("Invalid sort direction: " + sortDirection);
        }
    }

    /**
     * Check if sort field is valid
     */
    private boolean isValidSortField(String sortBy) {
        return sortBy.matches("(?i)(requestedAt|amount|status|createdAt|processedAt)");
    }
}
