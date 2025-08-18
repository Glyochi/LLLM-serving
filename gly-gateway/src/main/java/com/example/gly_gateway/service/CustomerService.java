package com.example.gly_gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.example.gly_gateway.model.Customer;

import reactor.core.publisher.Mono;

@Service
public class CustomerService {

  @Autowired
  private ReactiveMongoTemplate reactiveMongoTemplate;

  public Mono<Customer> saveCustomer(Customer customer) {
    return reactiveMongoTemplate.save(customer);
  }

  public Mono<Customer> getCustomerById(String customerId) {
    Criteria criteria = Criteria.where("id").is(customerId);
    Query query = Query.query(criteria);
    return reactiveMongoTemplate.findOne(query, Customer.class);
  }

}
