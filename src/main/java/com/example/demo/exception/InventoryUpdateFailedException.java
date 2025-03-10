package com.example.demo.exception;

public class InventoryUpdateFailedException extends RuntimeException {

  public InventoryUpdateFailedException(String message) {
    super(message);
  }
}
