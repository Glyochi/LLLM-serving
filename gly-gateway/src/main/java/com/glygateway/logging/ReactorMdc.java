package com.glygateway.logging;

import org.slf4j.MDC;
import reactor.core.publisher.Signal;

import java.util.Map;
import java.util.function.Consumer;

// Mdc = Mapped Diagnostic Context
public final class ReactorMdc {
    private ReactorMdc() {}

    public static <T> Consumer<Signal<T>> mdcLogger(Map<String, String> kv) {
        return signal -> {
            if (!signal.isOnNext() && !signal.isOnComplete() && !signal.isOnError()) return;
            if (kv == null || kv.isEmpty()) return;
            kv.forEach(MDC::put);
        };
    }

    public static void with(Map<String, String> kv, Runnable r) {
        Map<String, String> old = MDC.getCopyOfContextMap();
        try {
            if (kv != null) kv.forEach(MDC::put);
            r.run();
        } finally {
            MDC.clear();
            if (old != null) old.forEach(MDC::put);
        }
    }
}

