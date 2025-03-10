package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderEvent {
  private String orderId;
  private String status; // ORDER_CREATED, PAYMENT_SUCCESS, PAYMENT_FAILED, ROLLBACK
  private OrderRequest orderRequest;
}
