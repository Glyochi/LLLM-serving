package com.gly_gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gly_gateway.model.Order;
import com.gly_gateway.service.OrderService;

import reactor.core.publisher.Mono;

@SpringBootApplication
@RestController
@RequestMapping("/order")
public class OrderController {

  @Autowired
  private OrderService orderService;

  @PostMapping("/create")
  public Mono<Order> createOrder(@RequestBody Order order) {
    return orderService.saveOrder(order);
  }

  @GetMapping("/find-by-id")
  public Mono<Order> findOrderById(@RequestParam("orderId") String orderId) {
    return orderService.getOrderById(orderId);
  }


}
