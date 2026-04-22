package com.axioma.aion.identitygateway.adapter.in.web;

import com.axioma.aion.identitygateway.adapter.in.web.dto.CreateSessionRequest;
import com.axioma.aion.identitygateway.adapter.in.web.dto.CreateSessionResponse;
import com.axioma.aion.identitygateway.adapter.in.web.mapper.CreateSessionWebMapper;
import com.axioma.aion.identitygateway.domain.port.in.CreateSessionUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/internal/identity")
@RequiredArgsConstructor
@Slf4j
public class IdentitySessionController {

    private final CreateSessionUseCase createSessionUseCase;
    private final CreateSessionWebMapper createSessionWebMapper;

    @PostMapping("/session")
    public Mono<CreateSessionResponse> createSession(@RequestBody CreateSessionRequest request) {
        if (request == null) {
            log.warn("identity_session_create_request_invalid reason=request_body_null");
            return Mono.error(new IllegalArgumentException("Request body is required"));
        }

        log.info("identity_session_create_request_received authenticationId={}",
                request.authenticationId());

        return createSessionUseCase.createSession(createSessionWebMapper.toCommand(request))
                .doOnSuccess(response -> log.info(
                        "identity_session_create_request_completed authenticationId={} sessionId={} hasToken={}",
                        request.authenticationId(),
                        response != null ? response.sessionId() : null,
                        response != null && response.sessionToken() != null && !response.sessionToken().isBlank()))
                .doOnError(error -> log.error(
                        "identity_session_create_request_failed authenticationId={} message={}",
                        request.authenticationId(),
                        error.getMessage(),
                        error));
    }
}
