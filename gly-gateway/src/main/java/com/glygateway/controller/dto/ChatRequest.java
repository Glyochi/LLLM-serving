package com.glygateway.controller.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import com.glygateway.model.triton.Conversation;
import com.glygateway.model.triton.InferenceParams;

public record ChatRequest(
    @Valid @NotNull Conversation conversation,
    @Valid @NotNull InferenceParams inferenceParams) {
}
// public class ChatRequest {
// @Valid
// @NotNull
// Conversation conversation;
// @Valid
// @NotNull
// InferenceParams inferenceParams;
//
// public Conversation getConversation() {
// return conversation;
// }
//
// public InferenceParams getInferenceParams() {
// return inferenceParams;
// }
// }
