package com.glygateway.service.triton.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import inference.GRPCInferenceServiceGrpc.GRPCInferenceServiceStub;
import inference.GRPCInferenceServiceGrpc.GRPCInferenceServiceFutureStub;

import io.grpc.ManagedChannel;


@Configuration
public class TritonGrpcConfig {


  private final TritonConfig cfg;

  public TritonGrpcConfig(TritonConfig cfg){
    this.cfg = cfg;
  }

  @Bean(destroyMethod = "shutdownNow")
  public ManagedChannel tritonChannel() {
    return io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder
        .forAddress(cfg.host(), cfg.grpcPort())
        .usePlaintext() // or TLS
        .keepAliveTime(cfg.timeoutMs(), java.util.concurrent.TimeUnit.MILLISECONDS)
        .keepAliveWithoutCalls(true)
        .maxInboundMessageSize(64 * 1024 * 1024)
        .build();
  }

  // These two stubs will share the same channel because BEANNNN + IoC  
  @Bean
  public GRPCInferenceServiceStub tritonAsyncStub(
      ManagedChannel channel) {
    return inference.GRPCInferenceServiceGrpc.newStub(channel);
  }

  @Bean
  public GRPCInferenceServiceFutureStub tritonFutureStub(
      ManagedChannel channel) {
    return inference.GRPCInferenceServiceGrpc.newFutureStub(channel);
  }
}

