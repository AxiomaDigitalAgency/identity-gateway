package com.axioma.aion.identitygateway.adapter.in.web;

import com.axioma.aion.identitygateway.adapter.in.web.dto.AuthenticateRequest;
import com.axioma.aion.identitygateway.adapter.in.web.dto.AuthenticateResponse;
import com.axioma.aion.identitygateway.adapter.in.web.mapper.AuthenticateWebMapper;
import com.axioma.aion.identitygateway.domain.port.in.AuthenticateIdentityUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/internal/identity")
@RequiredArgsConstructor
@Slf4j
public class IdentityAuthenticationController {

    private final AuthenticateIdentityUseCase authenticateIdentityUseCase;
    private final AuthenticateWebMapper authenticateWebMapper;

    @PostMapping("/authenticate")
    public Mono<AuthenticateResponse> authenticate(@RequestBody AuthenticateRequest request) {
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

        log.info("identity_authenticate_request_received requestId={} authenticationType={} credentialId={} channel={}",
                requestId, authType, credentialId, channel);

        return authenticateIdentityUseCase.authenticate(authenticateWebMapper.toCommand(request))
                .doOnSuccess(response -> log.info(
                        "identity_authenticate_request_completed requestId={} authenticationId={} authenticated={}",
                        requestId,
                        response != null ? response.authenticationId() : null,
                        response != null && response.authenticated()))
                .doOnError(error -> log.error(
                        "identity_authenticate_request_failed requestId={} message={}",
                        requestId,
                        error.getMessage(),
                        error));
    }
}
