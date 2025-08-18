package com.example.gly_gateway.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.example.gly_gateway.model.Customer;
import com.example.gly_gateway.model.Order;

import reactor.core.publisher.Mono;

@Service
public class SaleService {

  @Autowired
  private ReactiveMongoTemplate reactiveMongoTemplate;

  public Mono<Map<String, Double>> caculateSummary() {
    return reactiveMongoTemplate.findAll(Customer.class)
        .flatMap(customer -> Mono.zip(Mono.just(customer), calculateCustomerOrderSummary(customer.getId())))
        .collectMap(tuple2 -> tuple2.getT1().getName(), tuple2 -> tuple2.getT2());
  }

  public Mono<Double> calculateCustomerOrderSummary(String customerId) {
    Criteria criteria = Criteria.where("customerId").is(customerId);
    Query query = Query.query(criteria);
    return reactiveMongoTemplate.find(query, Order.class)
        .map(order -> order.getTotal())
        .reduce(0d, (a, b) -> a + b);
  }

}
