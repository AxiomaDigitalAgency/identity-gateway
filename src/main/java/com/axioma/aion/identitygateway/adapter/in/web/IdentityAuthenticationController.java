package com.axioma.aion.identitygateway.adapter.in.web;

import com.axioma.aion.identitygateway.adapter.in.web.dto.AuthenticateRequest;
import com.axioma.aion.identitygateway.adapter.in.web.dto.AuthenticateResponse;
import com.axioma.aion.identitygateway.adapter.in.web.dto.ChannelSessionRequest;
import com.axioma.aion.identitygateway.adapter.in.web.dto.ChannelSessionResponse;
import com.axioma.aion.identitygateway.application.command.AuthenticateAndCreateSessionCommand;
import com.axioma.aion.identitygateway.application.command.AuthenticateIdentityCommand;
import com.axioma.aion.identitygateway.application.result.AuthenticateAndCreateSessionResult;
import com.axioma.aion.identitygateway.application.result.AuthenticateIdentityResult;
import com.axioma.aion.identitygateway.application.service.AuthenticateAndCreateSessionService;
import com.axioma.aion.identitygateway.application.service.AuthenticateIdentityService;
import com.axioma.aion.securitycore.model.SecurityRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/identity")
@RequiredArgsConstructor
public class IdentityAuthenticationController {

    private final AuthenticateIdentityService authenticateIdentityService;
    private final AuthenticateAndCreateSessionService authenticateAndCreateSessionService;

    @PostMapping("/authenticate")
    public Mono<AuthenticateResponse> authenticate(@Valid @RequestBody AuthenticateRequest request) {
        return authenticateIdentityService.execute(
                        AuthenticateIdentityCommand.builder()
                                .securityRequest(toSecurityRequest(
                                        request.channel(),
                                        request.authType(),
                                        request.credentials(),
                                        request.metadata()
                                ))
                                .build()
                )
                .map(this::toAuthenticateResponse);
    }

    @PostMapping("/channel/session")
    public Mono<ChannelSessionResponse> createChannelSession(@Valid @RequestBody ChannelSessionRequest request) {
        return authenticateAndCreateSessionService.execute(
                        AuthenticateAndCreateSessionCommand.builder()
                                .securityRequest(toSecurityRequest(
                                        request.channel(),
                                        request.authType(),
                                        request.credentials(),
                                        request.metadata()
                                ))
                                .build()
                )
                .map(this::toChannelSessionResponse);
    }

    private SecurityRequest toSecurityRequest(
            String channel,
            String authType,
            java.util.Map<String, Object> credentials,
            java.util.Map<String, Object> metadata
    ) {
        return SecurityRequest.builder()
                .channel(channel)
                .authType(authType)
                .credentials(credentials)
                .metadata(metadata)
                .build();
    }

    private AuthenticateResponse toAuthenticateResponse(AuthenticateIdentityResult result) {
        return AuthenticateResponse.builder()
                .authenticated(result.authenticated())
                .authContext(result.authContext())
                .build();
    }

    private ChannelSessionResponse toChannelSessionResponse(AuthenticateAndCreateSessionResult result) {
        return ChannelSessionResponse.builder()
                .authenticated(result.authenticated())
                .sessionToken(result.sessionToken())
                .sessionId(result.sessionId())
                .expiresAt(result.expiresAt())
                .authContext(result.authContext())
                .build();
    }
}