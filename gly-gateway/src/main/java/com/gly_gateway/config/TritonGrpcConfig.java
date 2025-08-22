package com.gly_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.grpc.ManagedChannel;

@Configuration
public class TritonGrpcConfig {

  @Bean(destroyMethod = "shutdownNow")
  public ManagedChannel tritonChannel() {
    return io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder
        .forAddress("localhost", 8001)
        .usePlaintext() // or TLS
        .keepAliveTime(30, java.util.concurrent.TimeUnit.SECONDS)
        .keepAliveWithoutCalls(true)
        .maxInboundMessageSize(64 * 1024 * 1024)
        .build();
  }

  // These two stubs will share the same channel because BEANNNN + IoC
  
  @Bean
  public inference.GRPCInferenceServiceGrpc.GRPCInferenceServiceStub tritonAsyncStub(
      ManagedChannel channel) {
    return inference.GRPCInferenceServiceGrpc.newStub(channel);
  }

  @Bean
  public inference.GRPCInferenceServiceGrpc.GRPCInferenceServiceFutureStub tritonFutureStub(
      ManagedChannel channel) {
    return inference.GRPCInferenceServiceGrpc.newFutureStub(channel);
  }
}

