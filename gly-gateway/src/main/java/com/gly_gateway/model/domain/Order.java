package com.gly_gateway.model.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Order {

  @Id
  private String id;
  private String customerId;
  private Double total;
  private Double discount;

  public Order(String id, String customerId, Double total, Double discount) {
    this.id = id;
    this.customerId = customerId;
    this.total = total;
    this.discount = discount;
  }

  public Order() {
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getCustomerId() {
    return customerId;
  }

  public void setCustomerId(String customerId) {
    this.customerId = customerId;
  }

  public Double getTotal() {
    return total;
  }

  public void setTotal(Double total) {
    this.total = total;
  }

  public Double getDiscount() {
    return discount;
  }

  public void setDiscount(Double discount) {
    this.discount = discount;
  }
}
