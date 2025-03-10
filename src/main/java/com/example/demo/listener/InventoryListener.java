package com.example.demo.listener;

import java.util.Optional;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.example.demo.entity.Order;
import com.example.demo.model.OrderEvent;
import com.example.demo.repository.OrderRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class InventoryListener {

  private final OrderRepository orderRepository;

  public InventoryListener(OrderRepository orderRepository) {
    this.orderRepository = orderRepository;
  }

  @KafkaListener(topics = "inventory-topic", groupId = "order-group")
  public void updateOrder(OrderEvent event) {
    Optional<Order> orderOpt = orderRepository.findById(Long.parseLong(event.getOrderId()));
    if ("ROLLBACK".equals(event.getStatus()) || "PAYMENT_FAILED".equals(event.getStatus())) {
      orderOpt.ifPresent(order -> {
        order.setStatus("FAILED");
        orderRepository.save(order);
        log.info(">>> Rolled back order: {}", order.getId());
      });
    } else if ("ORDER_COMPLETED".equals(event.getStatus())) {
      orderOpt.ifPresent(order -> {
        order.setStatus("COMPLETED");
        orderRepository.save(order);
        log.info(">>> Completed order: {}", order.getId());
      });
    }
  }
}
