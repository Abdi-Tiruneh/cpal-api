package com.commercepal.apiservice.promotions.promo;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "promo_code_usages")
@Getter
@Setter
public class PromoCodeUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private PromoCode promoCode;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "usage_count")
    private Integer usageCount = 0;

    @Column(name = "last_used_at")
    private Timestamp lastUsedAt;
}
