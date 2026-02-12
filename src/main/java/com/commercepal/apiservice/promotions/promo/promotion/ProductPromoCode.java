package com.commercepal.apiservice.promotions.promo.promotion;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Setter
@Getter
@Entity
@Table(name = "promo_product_codes")
public class ProductPromoCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "Code", unique = true)
    private String code;

    @Column(columnDefinition = "TEXT")
    private String promoCodeDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "DiscountType")
    private DiscountType discountType;

    @Column(name = "DiscountAmount")
    private BigDecimal discountAmount;

    @Column(name = "StartDate")
    private Timestamp startDate;

    @Column(name = "EndDate")
    private Timestamp endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "PromoCodeStatus")
    private PromoCodeStatus promoCodeStatus;

    @Column(name = "ProductId")
    private Long productId;

    @Column(name = "SubProductId")
    private Long subProductId;

    @Enumerated(EnumType.STRING)
    @Column(name = "Owner")
    private ProductPromoCodeOwner owner;

    @Column(name = "MerchantId")
    private Long merchantId;

    @CreationTimestamp
    @Column(name = "CreatedDate")
    private Timestamp createdDate;

    @UpdateTimestamp
    @Column(name = "UpdatedDate")
    private Timestamp updatedDate;

    @Column(name = "UpdatedBy")
    private Long updatedBy;
}
