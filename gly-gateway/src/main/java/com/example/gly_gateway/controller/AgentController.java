package com.example.gly_gateway.controller;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.gly_gateway.model.AgentChatRequest;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
@RestController
@RequestMapping("/agent")
public class AgentController {

  @PostMapping(value = "/complete", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<String> completeChat(@RequestBody AgentChatRequest agentChatRequest) {
    return Flux.just("1", "2", "3", "4").delayElements(Duration.ofMillis(500));
  }


}
