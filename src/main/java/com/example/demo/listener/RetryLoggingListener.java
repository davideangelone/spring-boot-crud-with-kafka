package com.example.demo.listener;

import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RetryLoggingListener implements RetryListener {

  @Override
  public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
    // Called before the first attempt
    log.info("Retry attempt started for method: {}", context.getAttribute("context.name"));
    return true; // Continue with the retry
  }

  @Override
  public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
    // Called after the final attempt (success or failure)
    if (throwable != null) {
      log.info("Retry attempt failed for method: {}", context.getAttribute("context.name"));
    } else {
      log.info("Retry attempt succeeded for method: {}", context.getAttribute("context.name"));
    }
  }

  @Override
  public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
    // Called after each failed attempt
    int retryCount = context.getRetryCount();
    log.info("Retry attempt {} failed for method: {}", retryCount, context.getAttribute("context.name"));
  }
}