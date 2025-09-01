package com.glygateway.model.triton;

public enum ModelId {
  Gemma("gemma-2b-it_tensorrt_llm_bls"), Gemma3("gemma-3-1b-it_tensorrt_llm_bls");

  private final String value;

  private ModelId(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }

  @Override
  public String toString() {
    return value;
  }

}
