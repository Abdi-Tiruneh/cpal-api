package com.commercepal.apiservice.orders.api;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Value object for shipping address.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShippingAddress {

  private String street;
  private String city;
  private String state;
  private String zipCode;
  private String country;
  private String recipientName;
  private String phoneNumber;
}

