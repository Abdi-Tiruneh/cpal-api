package com.commercepal.apiservice.promotions.affiliate.commission;

import com.commercepal.apiservice.promotions.affiliate.user.Affiliate;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "affiliate_commissions",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_affiliate_commissions_order", columnNames = {"affiliate_id",
            "order_id"}),
        @UniqueConstraint(name = "uq_affiliate_commissions_signup", columnNames = {"affiliate_id",
            "customer_id", "commission_type"})},
    indexes = {@Index(name = "idx_affiliate_commissions_affiliate", columnList = "affiliate_id"),
        @Index(name = "idx_affiliate_commissions_order", columnList = "order_id"),
        @Index(name = "idx_affiliate_commissions_customer", columnList = "customer_id"),
        @Index(name = "idx_affiliate_commissions_type", columnList = "commission_type")})
@Getter
@Setter
public class AffiliateCommission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Affiliate who earns the commission
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "affiliate_id", nullable = false)
    private Affiliate affiliate;

    /**
     * Commission type: SIGNUP or ORDER
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "commission_type", nullable = false, length = 20)
    private CommissionType commissionType;

    /**
     * Optional: the order that generated the commission
     */
    @Column(name = "order_id")
    private Long orderId;

    /**
     * The customer linked to this commission (signup or purchaser)
     */
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    /**
     * Base amount (order total, or fixed signup bonus base)
     */
    @Column(name = "base_amount", precision = 19, scale = 4)
    private BigDecimal baseAmount;

    /**
     * Commission amount awarded
     */
    @Column(name = "commission_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal commissionAmount;

    /**
     * Payment status
     */
    @Column(name = "paid", nullable = false)
    private Boolean paid = false;

    /**
     * Audit fields
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new Timestamp(System.currentTimeMillis());
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Timestamp(System.currentTimeMillis());
    }
}
