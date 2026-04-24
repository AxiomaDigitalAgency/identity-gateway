package com.axioma.aion.identitygateway.config.observability;

import org.springframework.web.server.ServerWebExchange;

public final class TraceIdUtils {

    private TraceIdUtils() {
    }

    public static String getRequired(ServerWebExchange exchange) {
        Object traceId = exchange.getAttribute(TraceConstants.TRACE_ID_ATTRIBUTE);

        if (traceId == null) {
            throw new IllegalStateException("TraceId not found in exchange");
        }

        return traceId.toString();
    }
}