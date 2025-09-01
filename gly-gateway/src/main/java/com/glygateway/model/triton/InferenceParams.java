
package com.glygateway.model.triton;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

public class InferenceParams {
  @NotNull
  final ModelId modelId;

  final Integer maxTokens;

  final Float temperature;

  @Nullable
  final Boolean stream;

  public InferenceParams(Integer maxTokens, Float temperature, ModelId modelId, boolean stream) {
    this.modelId = modelId;
    this.maxTokens = maxTokens;
    this.temperature = temperature;
    this.stream = stream;
  }

  public ModelId getModelId() {
    return modelId;
  }

  public int getMaxTokens() {
    return maxTokens;
  }

  public Float getTemperature() {
    return temperature;
  }

  public Boolean getStream() {
	return stream;
  }


}
