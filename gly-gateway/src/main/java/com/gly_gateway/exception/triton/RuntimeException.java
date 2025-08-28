package com.gly_gateway.exception.triton;

public class RuntimeException extends Exception {

  public RuntimeException() {
    super("Something went wrong while inferencing.");
  }

  public RuntimeException(String message) {
    super(message);
  }

  public RuntimeException(String message, Throwable cause) {
    super(message, cause);
  }

}

