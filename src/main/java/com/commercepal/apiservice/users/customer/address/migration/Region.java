package com.commercepal.apiservice.users.customer.address.migration;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.sql.Timestamp;
import java.util.List;
import lombok.Data;

@Data
@Entity
public class Region {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID")
  private Integer id;
  @JsonIgnore
  @Column(name = "RegionCode")
  private String regionCode;
  @Column(name = "RegionName")
  private String regionName;
  @Column(name = "DeliveryAllowed", nullable = false)
  private final Boolean deliveryAllowed = true;
  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "CountryId", nullable = false)
  private Country country;
  @Column(name = "Status")
  private Integer status;
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
  @Column(name = "CreatedDate")
  private Timestamp createdDate;
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
  @Column(name = "updatedDate")
  private Timestamp updatedDate;
  @OneToMany(mappedBy = "region", cascade = CascadeType.ALL)
  private List<City> cities;
}
