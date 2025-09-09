
package com.glygateway.model.triton;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

public class InferenceParams {
  @NotNull
  final ModelId modelId;

  final Integer maxTokens;

  @Nullable
  final Float temperature;

  @Nullable
  final Float topP;

  @Nullable
  final Integer topK;

  @Nullable
  final Long seed;

  @Nullable
  final Boolean stream;

  @Nullable
  final Boolean excludeInputInOutput;

  public InferenceParams(Integer maxTokens, Float temperature, ModelId modelId, boolean stream, Boolean excludeInputInOutput, Long seed, Float topP, Integer topK) {
    this.modelId = modelId;
    this.maxTokens = maxTokens;
    this.temperature = temperature;
	this.topP = topP;
	this.topK = topK;
	this.seed = seed;
    this.stream = stream;
	this.excludeInputInOutput = excludeInputInOutput;
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

  public Boolean getExcludeInputInOutput() {
	return excludeInputInOutput;
  }

  public Long getSeed() {
	return seed;
  }

  public Float getTopP() {
	return topP;
  }

  public Integer getTopK() {
	return topK;
  }


}
