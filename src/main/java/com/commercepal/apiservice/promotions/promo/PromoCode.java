package com.commercepal.apiservice.promotions.promo;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(
        name = "promo_codes",
        indexes = {
                @Index(name = "idx_promo_codes_code", columnList = "code", unique = true)
        }
)
@Setter
@Getter
public class PromoCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    private DiscountType discountType; // PERCENTAGE, FIXED

    @Column(name = "discount_value", nullable = false)
    private BigDecimal discountValue;

    @Column(name = "minimum_order_amount")
    private BigDecimal minimumOrderAmount;

    @Enumerated(EnumType.STRING)
    private PromoCodeScope scope; // GLOBAL, CATEGORY, PRODUCT, CUSTOMER

    @Column(name = "applicable_product_id")
    private Long applicableProductId;      // for PRODUCT scope
    @Column(name = "applicable_category_id")
    private Long applicableCategoryId;     // for CATEGORY scope
    @Column(name = "applicable_customer_id")
    private Long applicableCustomerId;     // for CUSTOMER scope

    @Column(name = "start_date")
    private Timestamp startDate;
    @Column(name = "end_date")
    private Timestamp endDate;

    @Column(name = "total_usage_limit")
    private Integer totalUsageLimit;       // e.g., 100 total
    @Column(name = "per_customer_usage_limit")
    private Integer perCustomerUsageLimit; // e.g., 1 per customer

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_by")
    private Long createdBy; // admin or system

    @CreatedDate
    @Column(name = "created_at")
    private Timestamp createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Timestamp updatedAt;
}
