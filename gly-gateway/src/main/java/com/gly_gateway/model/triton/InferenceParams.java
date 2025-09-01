
package com.gly_gateway.model.triton;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

public class InferenceParams {
  @NotNull
  final ModelId modelId;

  @NotNull
  final int maxTokens;

  @Nullable
  final Float temperature;

  @Nullable
  final Boolean stream;

  public InferenceParams(int maxTokens, Float temperature, ModelId modelId, boolean stream) {
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
