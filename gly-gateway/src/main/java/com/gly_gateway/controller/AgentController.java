package com.gly_gateway.controller;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gly_gateway.exception.InferenceFailedException;
import com.gly_gateway.exception.ValidationException;
import com.gly_gateway.model.AgentChatRequest;
import com.gly_gateway.service.AgentService;

import jakarta.validation.Valid;
import reactor.core.publisher.Flux;

@SpringBootApplication
@RestController
@RequestMapping("/agent")
@Validated
public class AgentController {

  @Autowired
  AgentService agentService;

  @PostMapping(value = "/fail")
  public Flux<String> fail() throws InferenceFailedException {
    throw new InferenceFailedException("REEEEEEEEEEEE");
  }

  @PostMapping(value = "/complete", produces = MediaType.TEXT_EVENT_STREAM_VALUE)

  public Flux<String> completeChat(@RequestBody AgentChatRequest agentChatRequest) {
    return Flux.just("1", "2", "3", "4").delayElements(Duration.ofMillis(500));
  }

  @PostMapping(value = "/stream-complete", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<String> monoCompleteChat(@Valid @RequestBody AgentChatRequest agentChatRequest) throws ValidationException, InferenceFailedException {
    return agentService.agentComplete(agentChatRequest);
  }

  // @PostMapping(value = "/stream-complete-2", produces =
  // MediaType.TEXT_EVENT_STREAM_VALUE)
  // public Flux<String> monoCompleteChat(@RequestBody AgentChatRequest
  // agentChatRequest) {
  // return agentService.agentComplete(agentChatRequest.getPrompt());
  // }

}
