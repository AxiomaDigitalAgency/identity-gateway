package com.axioma.aion.identitygateway.application.service;

import com.axioma.aion.identitygateway.application.command.AuthenticateIdentityCommand;
import com.axioma.aion.identitygateway.application.result.AuthenticateIdentityResult;
import com.axioma.aion.identitygateway.domain.port.out.SecurityProviderResolver;
import com.axioma.aion.securitycore.exception.InvalidSecurityRequestException;
import com.axioma.aion.securitycore.port.SecurityProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticateIdentityService {

    private final SecurityProviderResolver securityProviderResolver;

    public Mono<AuthenticateIdentityResult> execute(AuthenticateIdentityCommand command) {
        log.info("authenticate identity request");
        if (command == null || command.securityRequest() == null) {
            return Mono.error(new InvalidSecurityRequestException("securityRequest must not be null"));
        }

        String channel = command.securityRequest().channel();
        String authType = command.securityRequest().authType();

        SecurityProvider provider = securityProviderResolver.resolve(channel, authType);

        return provider.authenticate(command.securityRequest())
                .map(result -> AuthenticateIdentityResult.builder()
                        .authenticated(result.authenticated())
                        .authContext(result.authContext())
                        .build());
    }
}