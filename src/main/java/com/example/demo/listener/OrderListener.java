package com.example.demo.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.CircuitBreaker;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import com.example.demo.exception.PaymentFailedException;
import com.example.demo.model.OrderEvent;
import com.example.demo.service.KafkaService;
import com.example.demo.service.PaymentService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OrderListener {

  private final KafkaService kafkaService;
  private final PaymentService paymentService;

  public OrderListener(KafkaService kafkaService, PaymentService paymentService) {
    this.kafkaService = kafkaService;
    this.paymentService = paymentService;
  }

  @CircuitBreaker(label = "orderListener", recover = "fallbackPayment")
  @Retryable(retryFor = PaymentFailedException.class, maxAttempts = 5, backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 5000))
  @KafkaListener(topics = "order-topic", groupId = "payment-group")
  public void processPayment(OrderEvent event) {
    if ("ORDER_CREATED".equals(event.getStatus())) {
      boolean paymentSuccess = paymentService.attemptPayment(event.getOrderRequest().getPrice());

      if (paymentSuccess) {
        event.setStatus("PAYMENT_SUCCESS");
      } else {
        throw new PaymentFailedException("Payment failed for order: " + event.getOrderId());
      }

      log.info(">>> Order created and payment success: sending event to payment-topic: {}", event);
      kafkaService.notifyPayment(event);
    }
  }

  @Recover
  public void fallbackPayment(PaymentFailedException exception, OrderEvent event) {
    event.setStatus("PAYMENT_FAILED");
    log.info("Fallback: Payment failed for order. Trying kafka again.");
    kafkaService.notifyPayment(event);
    log.info("Fallback: Payment failed for order. Kafka message sent.");
  }

}
