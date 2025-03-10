package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.model.OrderRequest;
import com.example.demo.model.OrderResponse;
import com.example.demo.service.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/orders")
@Tag(name = "Order Management", description = "APIs for managing orders")
public class OrderController {

  private final OrderService orderService;
  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @PostMapping(value = "/place", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Place a new order", description = "Creates an order and returns the order details")
  public ResponseEntity<OrderResponse> placeOrder(@RequestBody OrderRequest request) {
    return ResponseEntity.ok(orderService.createOrder(request));
  }
}
