package com.axioma.aion.identitygateway.adapter.in.web;

import com.axioma.aion.identitygateway.adapter.in.web.dto.AuthenticateRequest;
import com.axioma.aion.identitygateway.adapter.in.web.dto.AuthenticateResponse;
import com.axioma.aion.identitygateway.adapter.in.web.mapper.AuthenticateWebMapper;
import com.axioma.aion.identitygateway.config.observability.LogEvents;
import com.axioma.aion.identitygateway.config.observability.TraceIdUtils;
import com.axioma.aion.identitygateway.domain.port.in.AuthenticateIdentityUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/internal/identity")
@RequiredArgsConstructor
@Slf4j
public class IdentityAuthenticationController {

    private final AuthenticateIdentityUseCase authenticateIdentityUseCase;
    private final AuthenticateWebMapper authenticateWebMapper;

    @PostMapping("/authenticate")
    public Mono<AuthenticateResponse> authenticate(@RequestBody AuthenticateRequest request, ServerWebExchange exchange) {
        String traceId = TraceIdUtils.getRequired(exchange);
        log.info("event={} traceId={} channel={} subject={}",
                LogEvents.AUTHENTICATION_REQUEST,
                traceId,
                request.channelContext().channel(),
                request.subjectContext().subject()
        );
        String requestId = request != null && request.requestMetadata() != null
                ? request.requestMetadata().requestId()
                : null;
        String authType = request != null && request.authenticationType() != null
                ? request.authenticationType().name()
                : null;
        String credentialId = request != null
                && request.credential() != null
                && request.credential().credentialId() != null
                ? request.credential().credentialId().toString()
                : null;
        String channel = request != null && request.channelContext() != null
                ? request.channelContext().channel()
                : null;

        log.info("event={} traceId={} requestId={} authenticationType={} credentialId={} channel={}",
                LogEvents.AUTHENTICATION_REQUEST, traceId, requestId, authType, credentialId, channel);

        return authenticateIdentityUseCase.authenticate(authenticateWebMapper.toCommand(request))
                .doOnSuccess(response -> log.info(
                        "event={} traceId={} tenantId={} subject={}",
                        LogEvents.AUTHENTICATION_SUCCESS,
                        traceId,
                        response.tenantId(),
                        response.subject()
                ))
                .doOnError(error -> log.error(
                        "event={} traceId={} error={}",
                        LogEvents.ERROR,
                        traceId,
                        error.getMessage()
                ));
    }
}
