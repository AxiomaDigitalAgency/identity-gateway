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
import com.axioma.aion.identitygateway.domain.port.out.JwtSessionProviderPort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
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
    public Mono<ValidateSessionResponse> validate(@Valid @RequestBody ValidateSessionRequest request) {
        log.info("session_controller_validate_request_received sessionTokenPresent={}",
                request != null && request.sessionToken() != null && !request.sessionToken().isBlank());

        return validateSessionService.execute(
                        ValidateSessionCommand.builder()
                                .sessionToken(request.sessionToken())
                                .build()
                )
                .map(this::toValidateResponse)
                .doOnSuccess(response -> log.info(
                        "session_controller_validate_request_completed valid={}",
                        response != null && response.valid()))
                .doOnError(error -> log.error(
                        "session_controller_validate_request_failed message={}",
                        error.getMessage(),
                        error));
    }

    @PostMapping("/revoke")
    public Mono<RevokeSessionResponse> revoke(@Valid @RequestBody RevokeSessionRequest request) {
        log.info("session_controller_revoke_request_received sessionTokenPresent={} reason={} requestedBy={}",
                request != null && request.sessionToken() != null && !request.sessionToken().isBlank(),
                request != null ? request.reason() : null,
                request != null ? request.requestedBy() : null);

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
                        "session_controller_revoke_request_completed sessionId={} revoked={}",
                        response != null ? response.sessionId() : null,
                        response != null && response.revoked()))
                .doOnError(error -> log.error(
                        "session_controller_revoke_request_failed message={}",
                        error.getMessage(),
                        error));
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
