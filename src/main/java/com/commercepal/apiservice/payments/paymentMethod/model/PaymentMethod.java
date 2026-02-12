package com.commercepal.apiservice.payments.paymentMethod.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.sql.Timestamp;
import lombok.*;

@Data
@Entity
public class PaymentMethod {

  @Id
  @Column(name = "Id")
  private Integer id;
  @Column(name = "Name")
  private String name;
  @Column(name = "PaymentMethod")
  private String paymentMethod;
  @Column(name = "UserType")
  private String userType;
  @Column(name = "IconUrl")
  private String iconUrl;
  @Column(name = "Status")
  private Integer status;
  @Column(name = "CreatedDate")
  private Timestamp createdDate;

}
