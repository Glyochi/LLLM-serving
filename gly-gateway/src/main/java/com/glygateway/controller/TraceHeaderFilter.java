package com.glygateway.controller;
import io.opentelemetry.api.trace.Span;
import reactor.core.publisher.Mono;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.*;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class TraceHeaderFilter implements WebFilter {
  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    var ctx = Span.current().getSpanContext();
    if (ctx.isValid()) {
      exchange.getResponse().getHeaders().set("trace-id", ctx.getTraceId());
      exchange.getResponse().getHeaders().set("span-id", ctx.getSpanId());
    }
    return chain.filter(exchange);
  }
}
