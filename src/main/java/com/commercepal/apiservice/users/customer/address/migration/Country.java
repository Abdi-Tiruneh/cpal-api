package com.commercepal.apiservice.users.customer.address.migration;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.sql.Timestamp;
import lombok.Data;

@Data
@Entity
public class Country {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID")
  private Long id;

  @Column(name = "Name")
  private String name;

  @Column(name = "CountryCode")
  private String countryCode;

  @Column(length = 10)
  private String phoneCode; // e.g., "+1", "+251"

  @Column(name = "Status")
  private Integer status;

  @Column(name = "CreatedDate")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
  private Timestamp createdDate;

  @Column(name = "UpdatedAt")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
  private Timestamp updatedAt;

  @Column(name = "DeliveryAllowed", nullable = false)
  private final Boolean deliveryAllowed = true;
}


