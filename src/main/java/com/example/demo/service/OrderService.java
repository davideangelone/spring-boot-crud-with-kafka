package com.example.demo.service;

import org.springframework.stereotype.Service;

import com.example.demo.entity.Order;
import com.example.demo.model.OrderEvent;
import com.example.demo.model.OrderRequest;
import com.example.demo.model.OrderResponse;
import com.example.demo.repository.OrderRepository;

import jakarta.transaction.Transactional;

@Service
public class OrderService {

  private final OrderRepository orderRepository;
  private final KafkaService kafkaService;

  public OrderService(OrderRepository orderRepository, KafkaService kafkaService) {
    this.orderRepository = orderRepository;
    this.kafkaService = kafkaService;
  }

  @Transactional
  public OrderResponse createOrder(OrderRequest request) {
    Order order = new Order(
            null, // ID is auto-generated
            request.getProductId(),
            request.getQuantity(),
            request.getPrice(),
            "CREATED"
    );
    Order savedOrder = orderRepository.save(order);

    OrderEvent event = new OrderEvent(savedOrder.getId().toString(), "ORDER_CREATED", request);
    kafkaService.notifyOrder(event);

    return new OrderResponse(savedOrder.getId());
  }

  /**
   * Simulate an attempt to update stock.
   *
   * @param productId Id of the product
   * @return true if update is successful, false otherwise.
   */
  public boolean updateStock(String productId) {
    // Randomly simulate a 80% success rate for update
    return Math.random() > 0.2; // 80% success rate
  }
}
