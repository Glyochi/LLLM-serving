package com.glygateway.service.triton.api;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.glygateway.model.triton.Conversation;
import com.glygateway.model.triton.InferenceParams;
import com.glygateway.model.triton.ModelId;
import com.glygateway.exception.triton.ValidationException;
import inference.GrpcService.ModelInferRequest;

public interface ModelAdapter {
  ModelId modelId(); // e.g., "gemma-2b-it"
  // boolean supports(ModelType type); // or by name

  // Build request to backend from a normalized conversation
  Mono<String> debugApplyChatTemplate(Conversation agentChatRequest) throws ValidationException;
  Mono<ModelInferRequest> buildRequest(Conversation agentChatRequest, InferenceParams inferenceParams) throws ValidationException;

  // Stream tokens from backend
  Flux<String> stream(ModelInferRequest request);

}
