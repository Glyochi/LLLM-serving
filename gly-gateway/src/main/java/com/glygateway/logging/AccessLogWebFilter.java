package com.glygateway.logging;

import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class AccessLogWebFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger("ACCESS_LOG");

    public static final String CORR_ID_KEY = "correlationId";
    private static final String HDR_TRACEPARENT = "traceparent";
    private static final String HDR_B3_TRACEID = "x-b3-traceid";
    private static final String HDR_REQUEST_ID  = "x-request-id";
    private static final String HDR_CORRELATION = "x-correlation-id";
    private static final String HDR_USER_AGENT  = "user-agent";
    private static final String HDR_XFF         = "x-forwarded-for";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        final ServerHttpRequest req = exchange.getRequest();
        final ServerHttpResponse res = exchange.getResponse();

        final String correlationId = resolveCorrelationId(req);
        // attach to response for clients
        res.getHeaders().set(HDR_CORRELATION, correlationId);

        final Instant start = Instant.now();

        return chain.filter(exchange)
            // ensure we log after the status is known but before commit finishes
            .doOnEach(sig -> {
                // keep MDC present for any mid-stream logs if you choose
                MDC.put(CORR_ID_KEY, correlationId);
            })
            .doFinally(signalType -> {
                try {
                    long status = Optional.ofNullable(res.getRawStatusCode()).orElse(200);
                    Duration took = Duration.between(start, Instant.now());

                    Map<String, Object> fields = baseFields(req, res, correlationId);
                    fields.put("status", status);
                    fields.put("duration_ms", took.toMillis());

                    // You can add response length if set by handler
                    long contentLength = res.getHeaders().getContentLength();
                    if (contentLength >= 0) fields.put("bytes_out", contentLength);

                    // single, structured JSON log line
                    log.info("access {} {} {} {} {} {} {} {} {} {} {} {} {}",
                        StructuredArguments.keyValue("method", fields.get("method")),
                        StructuredArguments.keyValue("path", fields.get("path")),
                        StructuredArguments.keyValue("status", fields.get("status")),
                        StructuredArguments.keyValue("duration_ms", fields.get("duration_ms")),
                        StructuredArguments.keyValue("remote_ip", fields.get("remote_ip")),
                        StructuredArguments.keyValue("user_agent", fields.get("user_agent")),
                        StructuredArguments.keyValue("host", fields.get("host")),
                        StructuredArguments.keyValue("scheme", fields.get("scheme")),
                        StructuredArguments.keyValue("query", fields.get("query")),
                        StructuredArguments.keyValue("bytes_in", fields.get("bytes_in")),
                        StructuredArguments.keyValue("bytes_out", fields.getOrDefault("bytes_out", 0)),
                        StructuredArguments.keyValue("correlation_id", fields.get("correlation_id")),
                        StructuredArguments.keyValue("trace_id", fields.get("trace_id"))
                    );
                } finally {
                    MDC.clear();
                }
            });
    }

    private static String resolveCorrelationId(ServerHttpRequest req) {
        // Prefer real tracing headers, then fall back to request/correlation ids, else new UUID
        return Optional.ofNullable(req.getHeaders().getFirst(HDR_TRACEPARENT)) // W3C (format "00-<traceId>-<spanId>-01")
                       .map(h -> h.split("-").length > 1 ? h.split("-")[1] : null)
                       .filter(s -> s != null && !s.isBlank())
              .or(() -> Optional.ofNullable(req.getHeaders().getFirst(HDR_B3_TRACEID)))
              .or(() -> Optional.ofNullable(req.getHeaders().getFirst(HDR_REQUEST_ID)))
              .or(() -> Optional.ofNullable(req.getHeaders().getFirst(HDR_CORRELATION)))
              .orElse(UUID.randomUUID().toString().replace("-", ""));
    }

    private static Map<String, Object> baseFields(ServerHttpRequest req, ServerHttpResponse res, String corrId) {
        Map<String, Object> m = new LinkedHashMap<>();
        String path   = req.getURI().getPath();
        String query  = Optional.ofNullable(req.getURI().getQuery()).orElse("");
        String method = req.getMethod().toString();
        String host   = Optional.ofNullable(req.getHeaders().getHost()).map(Object::toString).orElse("");
        String scheme = req.getURI().getScheme();
        String ua     = Optional.ofNullable(req.getHeaders().getFirst(HDR_USER_AGENT)).orElse("");
        String xff    = Optional.ofNullable(req.getHeaders().getFirst(HDR_XFF)).orElse("");
        InetSocketAddress remote = req.getRemoteAddress();
        String ip = !xff.isBlank() ? xff.split(",")[0].trim()
                : remote != null ? Optional.ofNullable(remote.getAddress()).map(Object::toString).orElse("") : "";

        long bytesIn = req.getHeaders().getContentLength();

        m.put("method", method);
        m.put("path", path);
        m.put("query", query);
        m.put("host", host);
        m.put("scheme", scheme);
        m.put("user_agent", ua);
        m.put("remote_ip", ip);
        m.put("bytes_in", Math.max(bytesIn, 0));
        m.put("correlation_id", corrId);

        // If Micrometer Tracing/Brave is active, these MDC keys are typically present:
        m.put("trace_id", Optional.ofNullable(MDC.get("traceId")).orElse(corrId));
        return m;
    }
}

