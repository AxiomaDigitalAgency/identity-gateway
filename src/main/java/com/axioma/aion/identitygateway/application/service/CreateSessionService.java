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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
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
    private final ObjectMapper objectMapper;

    public Mono<CreateSessionResult> execute(CreateSessionCommand command) {
        AuthContext authContext = command.authContext();

        if (authContext == null || !authContext.isValidForSession()) {
            return Mono.error(new InvalidSecurityRequestException("Invalid auth context for session creation"));
        }

        String identityContextId = readIdentityContextId(authContext);
        if (identityContextId == null || identityContextId.isBlank()) {
            return Mono.error(new InvalidSecurityRequestException("identityContextId must not be blank"));
        }

        OffsetDateTime issuedAt = clockPort.now();
        OffsetDateTime expiresAt = issuedAt.plusSeconds(sessionProperties.getTtlSeconds());

        IdentitySession identitySession = IdentitySession.builder()
                .id(idGeneratorPort.generateSessionId())
                .identityContextId(identityContextId)
                .tenantId(authContext.tenantId())
                .channel(normalizeChannelForStorage(authContext.channel()))
                .tokenId(new TokenId(idGeneratorPort.generateTokenId()))
                .status("ACTIVE")
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .lastSeenAt(null)
                .clientIp(null)
                .userAgent(null)
                .metadataJson(buildMetadataJson(authContext))
                .build();

        log.info(
                "create_session_attempt id={} identityContextId={} tenantId={} channel={} tokenId={} status={}",
                identitySession.id(),
                identitySession.identityContextId(),
                identitySession.tenantId(),
                identitySession.channel(),
                identitySession.tokenId().value(),
                identitySession.status()
        );

        return identitySessionRepositoryPort.save(identitySession)
                .flatMap(saved ->
                        jwtSessionProviderPort.generate(saved)
                                .flatMap(sessionToken ->
                                        sessionCachePort.save(
                                                        saved,
                                                        Duration.ofSeconds(sessionProperties.getTtlSeconds())
                                                )
                                                .then(auditEventPort.recordSessionCreated(
                                                        saved.tenantId(),
                                                        saved.id(),
                                                        saved.tokenId().value(),
                                                        authContext.subject(),
                                                        authContext.channel(),
                                                        authContext.provider()
                                                ))
                                                .thenReturn(CreateSessionResult.builder()
                                                        .sessionToken(sessionToken)
                                                        .sessionId(saved.id())
                                                        .tokenId(saved.tokenId())
                                                        .expiresAt(saved.expiresAt())
                                                        .authContext(authContext)
                                                        .build())
                                )
                )
                .doOnError(error -> log.error(
                        "create_session_error tenantId={} identityContextId={} channel={} message={}",
                        identitySession.tenantId(),
                        identitySession.identityContextId(),
                        identitySession.channel(),
                        error.getMessage(),
                        error
                ));
    }

    private String readIdentityContextId(AuthContext authContext) {
        Object value = authContext.safeAttributes().get("identityContextId");
        return value == null ? null : String.valueOf(value);
    }

    private String normalizeChannelForStorage(String channel) {
        return channel == null ? null : channel.toUpperCase();
    }

    private String buildMetadataJson(AuthContext authContext) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("subject", authContext.subject());
        metadata.put("provider", authContext.provider());
        metadata.put("authorities", authContext.safeAuthorities());
        metadata.put("attributes", authContext.safeAttributes());

        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize session metadata", ex);
        }
    }
}