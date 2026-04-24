package com.axioma.aion.identitygateway.adapter.in.web;

import com.axioma.aion.identitygateway.adapter.in.web.dto.RevokeSessionRequest;
import com.axioma.aion.identitygateway.adapter.in.web.dto.RevokeSessionResponse;
import com.axioma.aion.identitygateway.adapter.in.web.dto.ValidateSessionRequest;
import com.axioma.aion.identitygateway.adapter.in.web.dto.ValidateSessionResponse;
import com.axioma.aion.identitygateway.application.command.RevokeSessionCommand;
import com.axioma.aion.identitygateway.application.command.ValidateSessionCommand;
import com.axioma.aion.identitygateway.application.result.RevokeSessionResult;
import com.axioma.aion.identitygateway.application.result.ValidateSessionResult;
import com.axioma.aion.identitygateway.application.service.RevokeSessionService;
import com.axioma.aion.identitygateway.application.service.ValidateSessionService;
import com.axioma.aion.identitygateway.config.observability.TraceIdUtils;
import com.axioma.aion.identitygateway.domain.port.out.JwtSessionProviderPort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/internal/identity/session")
@RequiredArgsConstructor
@Slf4j
public class SessionController {

    private final ValidateSessionService validateSessionService;
    private final RevokeSessionService revokeSessionService;
    private final JwtSessionProviderPort jwtSessionProviderPort;

    @PostMapping("/validate")
    public Mono<ValidateSessionResponse> validate(@Valid @RequestBody ValidateSessionRequest request
            ,ServerWebExchange exchange) {
        String traceId = TraceIdUtils.getRequired(exchange);
        log.info("SESSION_VALIDATE_REQUEST traceId={}", traceId);

        return validateSessionService.execute(
                        ValidateSessionCommand.builder()
                                .sessionToken(request.sessionToken())
                                .build()
                )
                .map(this::toValidateResponse)
                .doOnSuccess(response -> log.info(
                        "SESSION_VALIDATED traceId={} tenantId={} sessionId={}",
                        traceId,
                        response.authContext().tenantId(),
                        response.authContext().sessionId()
                ))
                .doOnError(error -> log.error(
                        "SESSION_VALIDATE_FAILED traceId={} error={}",
                        traceId,
                        error.getMessage()
                ));
    }

    @PostMapping("/revoke")
    public Mono<RevokeSessionResponse> revoke(@Valid @RequestBody RevokeSessionRequest request
            ,ServerWebExchange exchange) {
        String traceId = TraceIdUtils.getRequired(exchange);
        log.info("SESSION_REVOKE_REQUEST  traceId={}", traceId);

        return jwtSessionProviderPort.parse(request.sessionToken())
                .flatMap(claims -> revokeSessionService.execute(
                        RevokeSessionCommand.builder()
                                .sessionId(parseUuid(claims.sessionId()))
                                .reason(request.reason())
                                .requestedBy(request.requestedBy())
                                .build()
                ))
                .map(this::toRevokeResponse)
                .doOnSuccess(response -> log.info(
                        "SESSION_REVOKED  traceId={} revoked={} sessionId={}",
                        traceId,
                        response.revoked(),
                        response.sessionId()
                ))
                .doOnError(error -> log.error(
                        "SESSION_REVOKE_FAILED   traceId={} error={}",
                        traceId,
                        error.getMessage()
                ));
    }

    private ValidateSessionResponse toValidateResponse(ValidateSessionResult result) {
        return ValidateSessionResponse.builder()
                .valid(true)
                .authContext(result.authContext())
                .build();
    }

    private RevokeSessionResponse toRevokeResponse(RevokeSessionResult result) {
        return RevokeSessionResponse.builder()
                .sessionId(result.sessionId())
                .revoked(result.revoked())
                .build();
    }

    private UUID parseUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid sessionId in token");
        }
    }
}
