package com.example.demo.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PaymentService {

  /**
   * Simulate an attempt to process payment.
   * @param price The price to be paid.
   * @return true if payment is successful, false otherwise.
   */
  public boolean attemptPayment(BigDecimal price) {
    // Randomly simulate a 70% success rate for payments
    return Math.random() > 0.3; // 70% success rate
  }
}
