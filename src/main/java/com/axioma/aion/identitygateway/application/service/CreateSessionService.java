package com.axioma.aion.identitygateway.application.service;

import com.axioma.aion.identitygateway.application.command.CreateSessionCommand;
import com.axioma.aion.identitygateway.application.result.CreateSessionResult;
import com.axioma.aion.identitygateway.config.SessionProperties;
import com.axioma.aion.identitygateway.domain.model.IdentitySession;
import com.axioma.aion.identitygateway.domain.model.valueobject.TokenId;
import com.axioma.aion.identitygateway.domain.port.out.AuditEventPort;
import com.axioma.aion.identitygateway.domain.port.out.ClockPort;
import com.axioma.aion.identitygateway.domain.port.out.IdGeneratorPort;
import com.axioma.aion.identitygateway.domain.port.out.IdentitySessionRepositoryPort;
import com.axioma.aion.identitygateway.domain.port.out.JwtSessionProviderPort;
import com.axioma.aion.identitygateway.domain.port.out.SessionCachePort;
import com.axioma.aion.securitycore.exception.InvalidSecurityRequestException;
import com.axioma.aion.securitycore.model.AuthContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class CreateSessionService {

    private final IdentitySessionRepositoryPort identitySessionRepositoryPort;
    private final SessionCachePort sessionCachePort;
    private final JwtSessionProviderPort jwtSessionProviderPort;
    private final ClockPort clockPort;
    private final IdGeneratorPort idGeneratorPort;
    private final AuditEventPort auditEventPort;
    private final SessionProperties sessionProperties;

    public Mono<CreateSessionResult> execute(CreateSessionCommand command) {
        AuthContext authContext = command.authContext();

        if (authContext == null || !authContext.isValidForSession()) {
            return Mono.error(new InvalidSecurityRequestException("Invalid auth context for session creation"));
        }

        OffsetDateTime issuedAt = clockPort.now();
        OffsetDateTime expiresAt = issuedAt.plusSeconds(sessionProperties.getTtlSeconds());

        IdentitySession identitySession = IdentitySession.builder()
                .sessionId(idGeneratorPort.generateSessionId())
                .tenantId(authContext.tenantId())
                .subject(authContext.subject())
                .channel(authContext.channel())
                .provider(authContext.provider())
                .tokenId(new TokenId(idGeneratorPort.generateTokenId()))
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .revokedAt(null)
                .authorities("[]")
                .attributes("{}")
                .build();

        return identitySessionRepositoryPort.save(identitySession)
                .flatMap(saved -> jwtSessionProviderPort.generate(saved)
                        .flatMap(sessionToken -> sessionCachePort.save(
                                                saved,
                                                Duration.ofSeconds(sessionProperties.getTtlSeconds())
                                        )
                                        .then(auditEventPort.recordSessionCreated(
                                                saved.tenantId(),
                                                saved.sessionId(),
                                                saved.tokenId().value(),
                                                saved.subject(),
                                                saved.channel(),
                                                saved.provider()
                                        ))
                                        .thenReturn(CreateSessionResult.builder()
                                                .sessionToken(sessionToken)
                                                .sessionId(saved.sessionId())
                                                .tokenId(saved.tokenId())
                                                .expiresAt(saved.expiresAt())
                                                .authContext(saved.toAuthContext())
                                                .build())
                        ));
    }
}