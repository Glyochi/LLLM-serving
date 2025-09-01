package com.glygateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.glygateway.model.domain.Customer;
import com.glygateway.service.domain.CustomerService;

import reactor.core.publisher.Mono;

@SpringBootApplication
@RestController
@RequestMapping("/customer")
public class CustomerController {

  @Autowired
  private CustomerService customerService;

  @PostMapping("/create")
  public Mono<Customer> createCustomer(@RequestBody Customer customer) {
    return customerService.saveCustomer(customer);
  }

  @GetMapping("/find-by-id")
  public Mono<Customer> findCustomerById(@RequestParam("customerId") String customerId) {
    return customerService.getCustomerById(customerId);
  }

}
