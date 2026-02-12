package com.commercepal.apiservice.users.customer.address.migration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "CustomerAddress")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerAddressOld {

  @Id
  @Column(name = "Id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "CustomerId")
  private Long customerId;

  @ManyToOne(optional = false)
  @JoinColumn(name = "CountryId")
  private Country country;

  @ManyToOne
  @JoinColumn(name = "RegionId")
  private Region region;

  @Column(name = "ManualRegionName", length = 100)
  private String manualRegionName;

  @ManyToOne
  @JoinColumn(name = "CityId")
  private City city;

  @Column(name = "ManualCityName", length = 100)
  private String manualCityName;

  @Column(name = "SubCity")
  private String subCity;

  @Column(name = "PhoneNumber")
  private String phoneNumber;

  @Column(name = "PhysicalAddress")
  private String physicalAddress;

  @Column(name = "Latitude")
  private String latitude;

  @Column(name = "Longitude")
  private String longitude;

  @Enumerated(EnumType.STRING)
  @Column(name = "Status", nullable = false)
  private final CustomerAddressStatus status = CustomerAddressStatus.ACTIVE;

  @Column(name = "apartment")
  private String apartment;

  @Column(name = "AdditionalNote")
  private String additionalNote;

  @Enumerated(EnumType.STRING)
  @Column(name = "AddressSource")
  private AddressSourceType addressSource;

  @Column(name = "IsDefault")
  private final Boolean isDefault = false;

  @Column(name = "CreatedDate")
  private Timestamp createdDate;

  @Column(name = "UpdatedDate")
  private Timestamp updatedDate;
}
