package com.glygateway.controller;

import java.util.HashMap;
import java.util.Map;

import reactor.core.observability.micrometer.Micrometer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.annotation.Observed;
import io.micrometer.tracing.annotation.NewSpan;
import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
@RestController
@RequestMapping("/agent")
@Validated
public class AgentController {

  @Autowired
  private ObservationRegistry observationRegistry;

  @Autowired
  private ModelAdapterRegistry modelRegistry;

  private static final Logger logger = LoggerFactory.getLogger(AgentController.class);

  @PostMapping(value = "/test/manual")
  public Mono<String> test_manual() {
    return Mono.defer(() -> {
      logger.info("TESTINGGGGGGGGGGGGGGGGGGGGGG");
      logger.info("REEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
      return Mono.just("Testing");
    }).name("/test/manual").tap(Micrometer.observation(observationRegistry));
  }

  @PostMapping(value = "/fail")
  public String fail() throws InferenceFailedException {
    throw new InferenceFailedException("REEEEEEEEEEEE");
  }

  @PostMapping(value = "/complete")
  public Mono<Map<String, String>> complete(@Valid @RequestBody ChatRequest request)
      throws ValidationException, InferenceFailedException {

    Map<String, String> result = new HashMap();
    var conversation = request.conversation();
    var inferenceParams = request.inferenceParams();
    var adapter = modelRegistry.forModelId(inferenceParams.getModelId());

    result.put("input", adapter.debugApplyChatTemplate(conversation));
    Flux<String> output_stream = adapter.stream(adapter.buildRequest(conversation, inferenceParams));
    return output_stream.reduce((a, b) -> a + b).map(concat -> {
      result.put("output", concat);
      return result;
    }).name("/complete").tap(Micrometer.observation(observationRegistry));

  }

  @PostMapping(value = "/stream-complete", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<String> streamComplete(@Valid @RequestBody ChatRequest request)
      throws ValidationException, InferenceFailedException {
    var conversation = request.conversation();
    var inferenceParams = request.inferenceParams();
    var adapter = modelRegistry.forModelId(inferenceParams.getModelId());

    return adapter.buildRequest2(conversation, inferenceParams).flatMapMany(adapter::stream);
  }

}
