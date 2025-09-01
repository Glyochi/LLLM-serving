package com.glygateway.exception.triton;

public class InferenceFailedException extends Exception {

  public InferenceFailedException() {
    super("Something went wrong on Triton.");
  }

  public InferenceFailedException(String message) {
    super(message);
  }

  public InferenceFailedException(String message, Throwable cause) {
    super(message, cause);
  }

}

