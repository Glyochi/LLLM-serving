package com.gly_gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gly_gateway.controller.dto.ChatRequest;
import com.gly_gateway.exception.triton.InferenceFailedException;
import com.gly_gateway.exception.triton.ValidationException;
import com.gly_gateway.service.triton.api.ModelAdapterRegistry;

import jakarta.validation.Valid;
import reactor.core.publisher.Flux;

@SpringBootApplication
@RestController
@RequestMapping("/agent")
@Validated
public class AgentController {

  @Autowired
  ModelAdapterRegistry registry;

  @PostMapping(value = "/fail")
  public Flux<String> fail() throws InferenceFailedException {
    throw new InferenceFailedException("REEEEEEEEEEEE");
  }

  @PostMapping(value = "/stream-complete", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<String> streamComplete(@Valid @RequestBody ChatRequest request)
      throws ValidationException, InferenceFailedException {
    var conversation = request.conversation();
    var inferenceParams = request.inferenceParams();
    var adapter = registry.forModelId(inferenceParams.getModelId());
    return adapter.stream(adapter.buildRequest(conversation, inferenceParams));
  }

}
