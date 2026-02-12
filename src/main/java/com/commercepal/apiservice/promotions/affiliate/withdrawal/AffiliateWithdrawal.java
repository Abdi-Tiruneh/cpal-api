package com.commercepal.apiservice.promotions.affiliate.withdrawal;

import com.commercepal.apiservice.promotions.affiliate.user.Affiliate;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "affiliate_withdrawals",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_affiliate_withdrawals_request",
                        columnNames = {"affiliate_id", "requested_at"}
                )
        },
        indexes = {
                @Index(name = "idx_affiliate_withdrawals_affiliate", columnList = "affiliate_id"),
                @Index(name = "idx_affiliate_withdrawals_status", columnList = "status"),
                @Index(name = "idx_affiliate_withdrawals_requested", columnList = "requested_at")
        })
@Getter
@Setter
public class AffiliateWithdrawal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The affiliate requesting the withdrawal */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "affiliate_id", nullable = false)
    @JsonIgnore
    private Affiliate affiliate;

    /** Amount requested for withdrawal */
    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    /** Payout channel type (Bank, TeleBirr, Amole, etc.) */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 50)
    private PaymentMethod paymentMethod;

    /** Account / wallet identifier */
    @Column(name = "account_number", nullable = false, length = 100)
    private String accountNumber;

    /** Bank or provider name */
    @Column(name = "bank_name", length = 100)
    private String bankName;

    /** Status of withdrawal */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private WithdrawalStatus status = WithdrawalStatus.PENDING;

    /** When affiliate requested payout */
    @Column(name = "requested_at", nullable = false, updatable = false)
    private Timestamp requestedAt;

    /** When it was processed (approved/rejected/paid) */
    @Column(name = "processed_at")
    private Timestamp processedAt;

    /** Additional fields for tracking and notes */
    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "admin_notes", length = 500)
    private String adminNotes;

    /** Audit timestamps */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    /** Audit fields for tracking who performed operations */
    @Column(name = "processed_by", length = 100)
    private String processedBy;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        createdAt = now;
        requestedAt = (requestedAt == null) ? now : requestedAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Timestamp(System.currentTimeMillis());
    }

    public enum WithdrawalStatus {
        PENDING,
        APPROVED,
        REJECTED,
        PAID
    }

    public enum PaymentMethod {
        BANK,
        TELEBIRR,
        EBIRR,
        OTHER
    }
}
