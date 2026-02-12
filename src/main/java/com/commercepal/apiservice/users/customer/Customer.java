package com.commercepal.apiservice.users.customer;

import com.commercepal.apiservice.shared.enums.Channel;
import com.commercepal.apiservice.shared.enums.SupportedCurrency;
import com.commercepal.apiservice.users.credential.Credential;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "customers",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_customers_account_number", columnNames = "account_number"),
        @UniqueConstraint(name = "uk_customers_commission_account", columnNames = "commission_account"),
        @UniqueConstraint(name = "uk_customers_referral_code", columnNames = "referral_code")
    },
    indexes = {
        @Index(name = "idx_customers_first_name", columnList = "first_name"),
        @Index(name = "idx_customers_last_name", columnList = "last_name"),
        @Index(name = "idx_customers_registration_channel", columnList = "registration_channel")
    }
)
public class Customer
//    extends BaseAuditEntity
{

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @CreatedBy
  @Column(name = "created_by", nullable = false, updatable = false, length = 100)
  private String createdBy;

  @LastModifiedBy
  @Column(name = "updated_by", length = 100)
  private String updatedBy;

  @Version
  @Column(name = "version", nullable = false)
  @Builder.Default
  private Long version = 0L;

  @Builder.Default
  @Column(name = "is_deleted", nullable = false)
  private Boolean isDeleted = false;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @Column(name = "deleted_by", length = 100)
  private String deletedBy;

  @Column(name = "created_ip", length = 45)
  private String createdIp;

  @Column(name = "updated_ip", length = 45)
  private String updatedIp;

  @Column(name = "remarks", length = 500)
  private String remarks;

  @Column(name = "old_customer_id", nullable = false)
  private Long oldCustomerId;

  @Column(name = "account_number", nullable = false, unique = true, length = 34)
  private String accountNumber;

  @Column(name = "commission_account", nullable = false, unique = true, length = 34)
  private String commissionAccount;

  @Column(name = "first_name", nullable = false, length = 120)
  private String firstName;

  @Column(name = "last_name", length = 120)
  private String lastName;

  @Column(name = "country", length = 3)
  private String country;

  @Column(name = "city", length = 120)
  private String city;

  @Column(name = "state_province", length = 50)
  private String stateProvince;

  @Builder.Default
  @Column(name = "preferred_language", length = 8)
  private String preferredLanguage = "en";

  @Builder.Default
  @Column(name = "preferred_currency", length = 3)
  @Enumerated(EnumType.STRING)
  private SupportedCurrency preferredCurrency = SupportedCurrency.ETB;

  @Column(name = "referral_code", length = 20)
  private String referralCode;

  @Builder.Default
  @Column(name = "registration_channel", nullable = false, length = 16)
  @Enumerated(EnumType.STRING)
  private Channel registrationChannel = Channel.WEB;

  @Column(name = "customer_notes", length = 1000)
  private String customerNotes;

  @Column(name = "admin_notes", length = 1000)
  private String adminNotes;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "credential_id", nullable = false, unique = true)
  private Credential credential;

  public Customer linkCredential(Credential credential) {
    this.credential = credential;
    return this;
  }

}
