package com.glygateway.service.triton.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import inference.GRPCInferenceServiceGrpc.GRPCInferenceServiceStub;
import inference.GRPCInferenceServiceGrpc.GRPCInferenceServiceFutureStub;
import io.grpc.Channel;
import io.grpc.ClientInterceptor;
import io.grpc.ClientInterceptors;
import io.grpc.ManagedChannel;

@Configuration
public class TritonGrpcConfig {

  private final TritonConfig cfg;
  private final ClientInterceptor grpcTracingClientInterceptor;

  public TritonGrpcConfig(TritonConfig cfg, ClientInterceptor tracingClientInterceptor) {
    this.cfg = cfg;
    this.grpcTracingClientInterceptor = tracingClientInterceptor;
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

  @Bean
  public Channel tracedChannel(ManagedChannel channel) {
    return ClientInterceptors.intercept(channel, grpcTracingClientInterceptor);
  }

  // These two stubs will share the same channel because BEANNNN + IoC
  @Bean
  public GRPCInferenceServiceStub tritonAsyncStub(
      Channel tracedChannel) {
    return inference.GRPCInferenceServiceGrpc.newStub(tracedChannel);
  }

  @Bean
  public GRPCInferenceServiceFutureStub tritonFutureStub(
      Channel tracedChannel) {
    return inference.GRPCInferenceServiceGrpc.newFutureStub(tracedChannel);
  }
}
