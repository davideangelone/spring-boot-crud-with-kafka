package com.example.demo.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.CircuitBreaker;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import com.example.demo.exception.InventoryUpdateFailedException;
import com.example.demo.model.OrderEvent;
import com.example.demo.service.KafkaService;
import com.example.demo.service.OrderService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PaymentListener {

  private final OrderService orderService;
  private final KafkaService kafkaService;

  public PaymentListener(OrderService orderService, KafkaService kafkaService) {
    this.orderService = orderService;
    this.kafkaService = kafkaService;
  }

  @CircuitBreaker(label = "paymentListener", recover = "fallbackInventory")
  @Retryable(retryFor = InventoryUpdateFailedException.class, maxAttempts = 5, backoff = @Backoff(delay = 2000))
  @KafkaListener(topics = "payment-topic", groupId = "inventory-group")
  public void updateInventory(OrderEvent event) {
    if ("PAYMENT_SUCCESS".equals(event.getStatus())) {
      boolean stockUpdated = orderService.updateStock(event.getOrderRequest().getProductId());

      if (stockUpdated) {
        event.setStatus("ORDER_COMPLETED");
      } else {
        throw new InventoryUpdateFailedException("Inventory update failed for product: " + event.getOrderRequest().getProductId());
      }

      log.info(">>> Payment success and order completed: sending event to inventory-topic: {}", event);
      kafkaService.notifyInventory(event);
    }
  }

  @Recover
  public void fallbackInventory(OrderEvent event, Throwable throwable) {
    event.setStatus("ROLLBACK");
    log.info("Fallback: Inventory failed for order. Trying kafka again.");
    kafkaService.notifyInventory(event);
    log.info("Fallback: Inventory failed for order. Kafka message sent.");
  }
}
