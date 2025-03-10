package com.example.demo.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.demo.model.OrderEvent;

@Service
public class KafkaService {

  private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

  public KafkaService(KafkaTemplate<String, OrderEvent> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void notifyOrder(OrderEvent event) {
    kafkaTemplate.send("order-topic", event.getOrderId(), event);
  }

  public void notifyPayment(OrderEvent event) {
    kafkaTemplate.send("payment-topic", event.getOrderId(), event);
  }

  public void notifyInventory(OrderEvent event) {
    kafkaTemplate.send("inventory-topic", event.getOrderId(), event);
  }
}
