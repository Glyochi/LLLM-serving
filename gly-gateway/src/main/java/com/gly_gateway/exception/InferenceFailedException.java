package com.gly_gateway.exception;

public class InferenceFailedException extends Exception {

  public InferenceFailedException() {
    super("Something went wrong while inferencing.");
  }

  public InferenceFailedException(String message) {
    super(message);
  }

  public InferenceFailedException(String message, Throwable cause) {
    super(message, cause);
  }

}

