package com.example.demo.entity;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String productId;
  private int quantity;
  private BigDecimal price;
  private String status; // ORDER_CREATED, PAYMENT_SUCCESS, ROLLBACK
}
