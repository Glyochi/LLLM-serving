package com.gly_gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gly_gateway.service.ModelService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
@RestController
@RequestMapping("/customer")
public class ModelController {

  @Autowired
  private ModelService customerService;

  @PostMapping("/complete")
  public Flux<String> createModel() {
    return Flux.just("ha", "hu");
  }

}
