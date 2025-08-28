package com.gly_gateway.exception.triton;

public class ValidationException extends Exception {

  public ValidationException() {
    super("Invalid input paramaters.");
  }

  public ValidationException(String message) {
    super(message);
  }

  public ValidationException(String message, Throwable cause) {
    super(message, cause);
  }

}

