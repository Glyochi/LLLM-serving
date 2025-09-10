package com.glygateway.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.glygateway.controller.dto.ChatRequest;
import com.glygateway.exception.triton.InferenceFailedException;
import com.glygateway.exception.triton.ValidationException;
import com.glygateway.service.triton.api.ModelAdapterRegistry;

import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

  @PostMapping(value = "/complete")
  public Mono<Map<String, String>> complete(@Valid @RequestBody ChatRequest request)
      throws ValidationException, InferenceFailedException {
    var conversation = request.conversation();
    var inferenceParams = request.inferenceParams();
    var adapter = registry.forModelId(inferenceParams.getModelId());
    Map<String, String> result = new HashMap(); 

    result.put("input", adapter.debugApplyChatTemplate(conversation));
    Flux<String> output_stream = adapter.stream(adapter.buildRequest(conversation, inferenceParams));
    return output_stream.reduce((a, b) -> a + b).map(concat -> {
      result.put("output", concat);
      return result;
    });
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
