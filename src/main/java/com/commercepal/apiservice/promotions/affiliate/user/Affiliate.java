package com.commercepal.apiservice.promotions.affiliate.user;

import com.commercepal.apiservice.promotions.affiliate.commission.Commission;
import com.commercepal.apiservice.users.credential.Credential;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "affiliates", indexes = {
    @Index(name = "idx_affiliates_referral_code", columnList = "referral_code"),
    @Index(name = "idx_affiliates_email", columnList = "email"),
    @Index(name = "idx_affiliates_phone", columnList = "phone_number")}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_affiliates_referral_code", columnNames = "referral_code"),
    @UniqueConstraint(name = "uk_affiliates_email", columnNames = "email"),
    @UniqueConstraint(name = "uk_affiliates_phone", columnNames = "phone_number")})
@Getter
@Setter
public class Affiliate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "referral_code", unique = true, nullable = false)
    private String referralCode;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "commission_type")
    private Commission commissionType; // PERCENTAGE or FIXED

    @Column(name = "commission_rate")
    private BigDecimal commissionRate; // percentage (0.10 for 10%) or fixed amount

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = false;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MMM dd,yyyy HH:mm")
    @CreatedDate
    @Column(name = "created_at")
    private Timestamp createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MMM dd,yyyy HH:mm")
    @LastModifiedDate
    @Column(name = "updated_at")
    private Timestamp updatedAt;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "credential_id", nullable = false, unique = true)
  private Credential credential;

  /**
   * Links the staff member to their account credentials.
   */
  public void linkCredential(Credential credential) {
    this.credential = credential;
  }
}
