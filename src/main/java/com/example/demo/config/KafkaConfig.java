package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import com.example.demo.model.OrderEvent;

@Configuration
public class KafkaConfig {

  @Autowired
  private KafkaTemplate<String, OrderEvent> kafkaTemplate;

  @Bean
  public DefaultErrorHandler errorHandler() {
    return new DefaultErrorHandler(new DeadLetterPublishingRecoverer(kafkaTemplate), new FixedBackOff(1000L, 2));
  }

}
