package com.axioma.aion.identitygateway.adapter.in.web;

import com.axioma.aion.identitygateway.adapter.in.web.dto.CreateSessionRequest;
import com.axioma.aion.identitygateway.adapter.in.web.dto.CreateSessionResponse;
import com.axioma.aion.identitygateway.adapter.in.web.mapper.CreateSessionWebMapper;
import com.axioma.aion.identitygateway.config.observability.LogEvents;
import com.axioma.aion.identitygateway.config.observability.TraceIdUtils;
import com.axioma.aion.identitygateway.domain.port.in.CreateSessionUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/internal/identity")
@RequiredArgsConstructor
@Slf4j
public class IdentitySessionController {

    private final CreateSessionUseCase createSessionUseCase;
    private final CreateSessionWebMapper createSessionWebMapper;

    @PostMapping("/session")
    public Mono<CreateSessionResponse> createSession(@RequestBody CreateSessionRequest request
            ,ServerWebExchange exchange) {
        String traceId = TraceIdUtils.getRequired(exchange);
        log.info("event={} traceId={} authenticationId={}",
                LogEvents.SESSION_CREATE_REQUEST,
                traceId,
                request.authenticationId()
        );
        return createSessionUseCase.createSession(createSessionWebMapper.toCommand(request))
                .doOnSuccess(response -> log.info(
                        "event={} traceId={} tenantId={} sessionId={}",
                        LogEvents.SESSION_CREATED,
                        traceId,
                        response.authContext().tenantId(),
                        response.authContext().sessionId()
                ))
                .doOnError(error -> log.error(
                        "event={} traceId={} error={}",
                        LogEvents.ERROR,
                        traceId,
                        error.getMessage()
                ));
    }
}
