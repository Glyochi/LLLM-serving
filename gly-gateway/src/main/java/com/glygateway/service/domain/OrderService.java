package com.glygateway.service.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.glygateway.model.domain.Order;

import reactor.core.publisher.Mono;

@Service
public class OrderService {

  @Autowired
  private ReactiveMongoTemplate reactiveMongoTemplate;

  public Mono<Order> saveOrder(Order order) {
    return reactiveMongoTemplate.save(order);
  }

  public Mono<Order> getOrderById(String orderId) {
    Criteria criteria = Criteria.where("id").is(orderId);
    Query query = Query.query(criteria);
    return reactiveMongoTemplate.findOne(query, Order.class);
  }

}
