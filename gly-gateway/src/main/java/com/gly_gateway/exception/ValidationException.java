package com.gly_gateway.exception;

public class ValidationException extends Exception {

  public ValidationException() {
    super("Something went wrong while inferencing.");
  }

  public ValidationException(String message) {
    super(message);
  }

  public ValidationException(String message, Throwable cause) {
    super(message, cause);
  }

}

