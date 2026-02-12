package com.commercepal.apiservice.users.customer.address.migration;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.sql.Timestamp;
import lombok.Data;

@Data
@Entity
public class City {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "CityId")
  private Long cityId;
  @Column(name = "CountryId")
  private Long countryId;
  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "RegionId", nullable = false)
  private Region region;
  @Column(name = "City")
  private String city;
  @Column(name = "Status")
  private Integer status;
  @Column(name = "DeliveryAllowed", nullable = false)
  private final Boolean deliveryAllowed = true;
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
  @Column(name = "CreatedDate")
  private Timestamp createdDate;
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
  @Column(name = "updatedDate")
  private Timestamp updatedDate;

}
