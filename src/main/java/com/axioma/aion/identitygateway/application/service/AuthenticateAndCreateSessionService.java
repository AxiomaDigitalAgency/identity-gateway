package com.axioma.aion.identitygateway.application.service;

import com.axioma.aion.identitygateway.application.command.AuthenticateAndCreateSessionCommand;
import com.axioma.aion.identitygateway.application.command.AuthenticateIdentityCommand;
import com.axioma.aion.identitygateway.application.command.CreateSessionCommand;
import com.axioma.aion.identitygateway.application.result.AuthenticateAndCreateSessionResult;
import com.axioma.aion.securitycore.exception.InvalidSecurityRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AuthenticateAndCreateSessionService {

    private final AuthenticateIdentityService authenticateIdentityService;
    private final CreateSessionService createSessionService;

    public Mono<AuthenticateAndCreateSessionResult> execute(AuthenticateAndCreateSessionCommand command) {
        if (command == null || command.securityRequest() == null) {
            return Mono.error(new InvalidSecurityRequestException("securityRequest must not be null"));
        }

        return authenticateIdentityService.execute(
                        AuthenticateIdentityCommand.builder()
                                .securityRequest(command.securityRequest())
                                .build()
                )
                .flatMap(authResult -> createSessionService.execute(
                        CreateSessionCommand.builder()
                                .authContext(authResult.authContext())
                                .build()
                ))
                .map(sessionResult -> AuthenticateAndCreateSessionResult.builder()
                        .authenticated(true)
                        .sessionToken(sessionResult.sessionToken())
                        .sessionId(sessionResult.sessionId())
                        .expiresAt(sessionResult.expiresAt())
                        .authContext(sessionResult.authContext())
                        .build());
    }
}