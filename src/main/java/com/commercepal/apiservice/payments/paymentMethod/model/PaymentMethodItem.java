package com.commercepal.apiservice.payments.paymentMethod.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.sql.Timestamp;
import lombok.*;

@Data
@Entity
public class PaymentMethodItem {

  @Id
  @Column(name = "Id")
  private Integer id;
  @Column(name = "PaymentMethodId")
  private Integer paymentMethodId;
  @Column(name = "UserType")
  private String userType;
  @Column(name = "Name")
  private String name;
  @Column(name = "PaymentType")
  private String paymentType;
  @Column(name = "PaymentCurrency")
  private String paymentCurrency;
  @Column(name = "IconUrl")
  private String iconUrl;
  @Column(name = "PaymentInstruction")
  private String paymentInstruction;
  @Column(name = "Status")
  private Integer status;
  @Column(name = "CreatedDate")
  private Timestamp createdDate;

}
