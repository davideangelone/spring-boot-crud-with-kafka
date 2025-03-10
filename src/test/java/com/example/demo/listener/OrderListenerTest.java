package com.example.demo.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.example.demo.exception.PaymentFailedException;
import com.example.demo.model.OrderEvent;
import com.example.demo.model.OrderRequest;
import com.example.demo.service.PaymentService;

@SpringBootTest
class OrderListenerTest {

  @Autowired
  private OrderListener orderListener;

  @MockitoBean
  private KafkaTemplate<String, OrderEvent> kafkaTemplate;

  @MockitoBean
  private PaymentService paymentService;

  private OrderEvent getOrderEvent() {
    OrderEvent event = new OrderEvent();
    event.setOrderId("1");
    event.setStatus("ORDER_CREATED");
    event.setOrderRequest(new OrderRequest("product123", 2, new BigDecimal("200.0")));
    return event;
  }

  @Test
  void testProcessPayment_Success() {
    OrderEvent event = getOrderEvent();

    // Mock attemptPayment to always return true (payment successful)
    when(paymentService.attemptPayment(any(BigDecimal.class))).thenReturn(true);

    orderListener.processPayment(event);

    assertEquals("PAYMENT_SUCCESS", event.getStatus());
  }

  @Test
  void testProcessPayment_Failure_Fallback_Success() {
    OrderEvent event = getOrderEvent();

    // Mock attemptPayment to always return false (payment failed)
    when(paymentService.attemptPayment(any(BigDecimal.class))).thenReturn(false);

    orderListener.processPayment(event);

    assertEquals("PAYMENT_FAILED", event.getStatus());
  }

  @Test
  void testProcessPayment_Failure_Fallback_Failure() {
    OrderEvent event = new OrderEvent("1", "ORDER_CREATED", new OrderRequest("product123", 2, new BigDecimal("200.0")));

    // Mock attemptPayment to always return false (payment failed)
    when(paymentService.attemptPayment(any(BigDecimal.class))).thenReturn(false);

    // Simulate kafka processing error
    when(kafkaTemplate.send(anyString(), any(OrderEvent.class))).thenThrow(new PaymentFailedException("Payment failed"));

    assertThrows(PaymentFailedException.class, () -> orderListener.processPayment(event));
  }

  @Test
  void testProcessPayment_Failure_Fallback_Failure2() {
    OrderEvent event = getOrderEvent();

    // Mock attemptPayment to always return false (payment failed)
    when(paymentService.attemptPayment(any(BigDecimal.class))).thenThrow(new PaymentFailedException("Payment failed"));

    // Simulate kafka processing error
    when(kafkaTemplate.send(anyString(), any(OrderEvent.class))).thenThrow(new PaymentFailedException("Payment failed"));

    assertThrows(PaymentFailedException.class, () -> orderListener.processPayment(event));
  }

  @Test
  void testProcessPayment_Retry_Success() {
    OrderEvent event = getOrderEvent();

    // Mock attemptPayment to throw exception for the first 2 attempts, then succeed on the 3rd
    when(paymentService.attemptPayment(any(BigDecimal.class)))
            .thenThrow(new PaymentFailedException("Payment failed")) // First attempt fails
            .thenThrow(new PaymentFailedException("Payment failed")) // Second attempt fails
            .thenReturn(true);  // Success on the third attempt

    orderListener.processPayment(event);

    // Verify that the payment was successful after retries
    assertEquals("PAYMENT_SUCCESS", event.getStatus());
    verify(kafkaTemplate).send(eq("payment-topic"), eq(event));  // Ensure Kafka message is sent
  }

  @Test
  void testProcessPayment_Retry_Success2() {
    OrderEvent event = getOrderEvent();

    // Simulate first two attempts failing, third attempt succeeding
    when(paymentService.attemptPayment(any(BigDecimal.class)))
            .thenReturn(false)
            .thenReturn(false)
            .thenReturn(true);

    orderListener.processPayment(event);

    verify(paymentService, times(3)).attemptPayment(any(BigDecimal.class));
    verify(kafkaTemplate, times(1)).send(eq("payment-topic"), eq(event));
  }

}
