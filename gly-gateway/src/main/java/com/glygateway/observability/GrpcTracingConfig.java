package com.glygateway.observability;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.grpc.*;
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;

// This is for propagating the traceId to the grpc calls to Triton, so triton's spans can be linked to the originated requests
@Configuration
class GrpcTracingConfig {

  @Bean
  ClientInterceptor grpcTracingClientInterceptor(Tracer tracer, Propagator propagator) {
    Propagator.Setter<Metadata> setter = (carrier, key, value) ->
        carrier.put(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER), value);

    return new ClientInterceptor() {
      @Override
      public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
          MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {

        // Capture the current span (it’s kept in Reactor context if context propagation enabled)
        Span current = tracer.currentSpan();

        return new SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {
          @Override
          public void start(Listener<RespT> responseListener, Metadata headers) {
            if (current != null) {
              propagator.inject(current.context(), headers, setter); // adds traceparent/tracestate
            }
            super.start(responseListener, headers);
          }
        };
      }
    };
  }
}

