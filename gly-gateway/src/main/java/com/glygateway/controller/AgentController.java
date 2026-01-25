package com.glygateway.controller;

import java.util.HashMap;
import java.util.Map;

import reactor.core.observability.micrometer.Micrometer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.glygateway.controller.dto.ChatRequest;
import com.glygateway.exception.triton.InferenceFailedException;
import com.glygateway.exception.triton.ValidationException;
import com.glygateway.service.triton.api.ModelAdapterRegistry;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@SpringBootApplication
@RestController
@RequestMapping("/agent")
@Validated
@CrossOrigin(origins = "http://glyml:5173")
public class AgentController {

  @Autowired
  private ObservationRegistry observationRegistry;

  @Autowired
  private MeterRegistry meterRegistry;

  @Autowired
  private ModelAdapterRegistry modelRegistry;

  private static final Logger logger = LoggerFactory.getLogger(AgentController.class);

  @PostMapping(value = "/test/manual")
  public Flux<String> test_manual() {
    return Flux.defer(() -> {
      logger.info("TESTINGGGGGGGGGGGGGGGGGGGGGG");
      logger.info("REEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
      return Flux.just("Testing", "AAHAHAH", "REEE");
    }).publishOn(Schedulers.parallel())
        .flatMap(v -> {
          logger.info("split");
          return Flux.just(v.split(""))
              .name("splitting")
              .tag("action", "splitting")
              .tap(Micrometer.observation(observationRegistry));
        })
        .publishOn(Schedulers.parallel())
        .flatMap(v -> {
          logger.info("append");
          return Flux.just(v + "__")
              .name("appending")
              .tag("action", "appending")
              .tap(Micrometer.observation(observationRegistry));
        });
  }

  @PostMapping(value = "/test/manual-2")
  public Flux<String> test_manual_2() {
    return Flux.deferContextual((context) -> {
      logger.info("TESTINGGGGGGGGGGGGGGGGGGGGGG");
      logger.info("REEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
      return Flux.just("Testing", "AAHAHAH", "REEE");
    }).publishOn(Schedulers.parallel())
        .flatMap(v -> {
          logger.info("split");
          return Flux.just(v.split(""));
        })
        .name("splitting")
        .tag("action", "splitting")
        .tap(Micrometer.observation(observationRegistry))
        .publishOn(Schedulers.parallel())
        .flatMap(v -> {
          logger.info("append");
          return Flux.just(v + "__");
        })
        .name("appending")
        .tag("action", "appending")
        .tap(Micrometer.observation(observationRegistry));
  }

  @PostMapping(value = "/fail")
  public String fail() throws InferenceFailedException {
    throw new InferenceFailedException("REEEEEEEEEEEE");
  }

  @PostMapping(value = "/complete")
  public Mono<Map<String, String>> complete(@Valid @RequestBody ChatRequest request)
      throws ValidationException, InferenceFailedException {

    var conversation = request.conversation();
    var inferenceParams = request.inferenceParams();
    var adapter = modelRegistry.forModelId(inferenceParams.getModelId());

    Mono<Map<String, String>> result = Mono.zip(
        adapter.debugApplyChatTemplate(conversation)
            .name("apply-chat-template")
            .tap(Micrometer.metrics(meterRegistry))
            .tap(Micrometer.observation(observationRegistry)),
        adapter.buildRequest(conversation, inferenceParams)
            .flatMapMany(adapter::stream)
            .reduce((a, b) -> a + b)
            .name("stream-tokens")
            .tap(Micrometer.metrics(meterRegistry))
            .tap(Micrometer.observation(observationRegistry)))
        .map(tuple -> {

          Map<String, String> final_result = new HashMap();
          final_result.put("input", tuple.getT1());
          final_result.put("output", tuple.getT2());

          return final_result;
        });

    return result;

  }

  @PostMapping(value = "/stream-complete", produces = MediaType.APPLICATION_NDJSON_VALUE)
  public Flux<Map<String, String>> streamComplete2(@Valid @RequestBody ChatRequest request)
      throws ValidationException, InferenceFailedException {
    var conversation = request.conversation();
    var inferenceParams = request.inferenceParams();
    var adapter = modelRegistry.forModelId(inferenceParams.getModelId());

    return adapter.buildRequest(conversation, inferenceParams)
        .name("apply-template")
        .tap(Micrometer.metrics(meterRegistry))
        .tap(Micrometer.observation(observationRegistry))
        .flatMapMany(v -> {
          return adapter.stream(v)
              .name("stream-tokens")
              .tap(Micrometer.metrics(meterRegistry))
              .tap(Micrometer.observation(observationRegistry));
        }).map(token -> Map.of("content", token));
  }

}
