package com.axioma.aion.identitygateway.config.observability;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdWebFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String traceId = resolveTraceId(exchange);

        exchange.getAttributes().put(TraceConstants.TRACE_ID_ATTRIBUTE, traceId);
        exchange.getResponse().getHeaders().set(TraceConstants.TRACE_ID_HEADER, traceId);

        return chain.filter(exchange)
                .contextWrite(context ->
                        context.put(TraceConstants.TRACE_ID_CONTEXT_KEY, traceId)
                )
                .doOnEach(signal -> {
                    if (!signal.isOnComplete() && !signal.isOnError()) {
                        MDC.put(TraceConstants.TRACE_ID_CONTEXT_KEY, traceId);
                    }
                })
                .doFinally(signalType -> MDC.remove(TraceConstants.TRACE_ID_CONTEXT_KEY));
    }

    private String resolveTraceId(ServerWebExchange exchange) {
        String incomingTraceId = exchange.getRequest()
                .getHeaders()
                .getFirst(TraceConstants.TRACE_ID_HEADER);

        if (StringUtils.hasText(incomingTraceId)) {
            return incomingTraceId;
        }

        return UUID.randomUUID().toString();
    }
}