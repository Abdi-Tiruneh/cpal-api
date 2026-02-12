package com.commercepal.apiservice.users.migration;

import com.commercepal.apiservice.shared.enums.SupportedCountry;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "Customer")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OldCustomer {

  @Id
  @Column(name = "CustomerId")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long customerId;
  @Column(name = "EmailAddress")
  private String emailAddress;
  @Column(name = "PhoneNumber")
  private String phoneNumber;
  @Column(name = "AccountNumber")
  private String accountNumber;
  @Column(name = "CommissionAccount")
  private String commissionAccount;
  @Column(name = "FirstName")
  private String firstName;
  @Column(name = "MiddleName")
  private String middleName;
  @Column(name = "LastName")
  private String lastName;
  @Column(name = "Language")
  private String language;
  @Column(name = "Country")
  private String country;
  @Column(name = "CountryIso")
  @Enumerated(EnumType.STRING)
  private SupportedCountry countryIso;
  @Column(name = "City")
  private String city;
  @Column(name = "District")
  private String district;
  @Column(name = "Location")
  private String location;
  @Column(name = "Status")
  private Integer status;
  @Column(name = "RegisteredBy")
  private String registeredBy;
  @Column(name = "RegisteredDate")
  private Timestamp registeredDate;
  @Column(name = "CreatedAt")
  private Timestamp createdAt;
  //ReferralCode
  @Column(name = "ReferralCode")
  private String referralCode;
  @Column(name = "OAuthUserId")
  private String OAuthUserId;
}







