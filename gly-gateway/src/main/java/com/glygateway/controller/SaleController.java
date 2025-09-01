package com.glygateway.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.glygateway.service.domain.SaleService;

import reactor.core.publisher.Mono;

@SpringBootApplication
@RestController
@RequestMapping("/sale")
public class SaleController {

  @Autowired
  private SaleService saleService;

  @GetMapping("/summary")
  public Mono<Map<String, Double>> calculateSummary() {
    return saleService.caculateSummary();
  }


}
