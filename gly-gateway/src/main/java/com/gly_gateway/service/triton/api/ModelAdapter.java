package com.gly_gateway.service.triton.api;

import reactor.core.publisher.Flux;

import com.gly_gateway.model.triton.Conversation;
import com.gly_gateway.model.triton.InferenceParams;
import com.gly_gateway.exception.triton.ValidationException;
import inference.GrpcService.ModelInferRequest;

public interface ModelAdapter {
  String modelId(); // e.g., "gemma-2b-it"
  // boolean supports(ModelType type); // or by name

  // Build request to backend from a normalized conversation
  ModelInferRequest buildRequest(Conversation agentChatRequest, InferenceParams inferenceParams) throws ValidationException;

  // Stream tokens from backend
  Flux<String> stream(ModelInferRequest request);

}
