package com.axioma.aion.identitygateway.application.service;

import com.axioma.aion.identitygateway.application.command.ValidateSessionCommand;
import com.axioma.aion.identitygateway.application.result.ValidateSessionResult;
import com.axioma.aion.identitygateway.domain.model.IdentitySession;
import com.axioma.aion.identitygateway.domain.model.JwtSessionClaims;
import com.axioma.aion.identitygateway.domain.port.out.AuditEventPort;
import com.axioma.aion.identitygateway.domain.port.out.ClockPort;
import com.axioma.aion.identitygateway.domain.port.out.IdentitySessionRepositoryPort;
import com.axioma.aion.identitygateway.domain.port.out.JwtSessionProviderPort;
import com.axioma.aion.identitygateway.domain.port.out.SessionCachePort;
import com.axioma.aion.securitycore.exception.AuthenticationFailedException;
import com.axioma.aion.securitycore.exception.InvalidSecurityRequestException;
import com.axioma.aion.securitycore.model.AuthContext;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ValidateSessionService {

    private final JwtSessionProviderPort jwtSessionProviderPort;
    private final SessionCachePort sessionCachePort;
    private final IdentitySessionRepositoryPort identitySessionRepositoryPort;
    private final ClockPort clockPort;
    private final AuditEventPort auditEventPort;
    private final ObjectMapper objectMapper;

    public Mono<ValidateSessionResult> execute(ValidateSessionCommand command) {
        if (command.sessionToken() == null || command.sessionToken().isBlank()) {
            return Mono.error(new InvalidSecurityRequestException("sessionToken must not be blank"));
        }

        return jwtSessionProviderPort.parse(command.sessionToken())
                .flatMap(claims -> resolveSession(claims)
                        .flatMap(session -> validateResolvedSession(claims, session))
                        .map(session -> toAuthContext(session, claims))
                )
                .map(authContext -> ValidateSessionResult.builder()
                        .authContext(authContext)
                        .build());
    }

    private Mono<IdentitySession> resolveSession(JwtSessionClaims claims) {
        return sessionCachePort.findByTokenId(claims.tokenId())
                .switchIfEmpty(Mono.defer(() ->
                        identitySessionRepositoryPort.findBySessionTokenId(claims.tokenId())
                ))
                .switchIfEmpty(Mono.error(new AuthenticationFailedException("Session not found")));
    }

    private Mono<IdentitySession> validateResolvedSession(JwtSessionClaims claims, IdentitySession session) {
        SessionMetadata metadata = readMetadata(session.metadataJson(), claims.subject(), claims.provider());

        if (!session.tokenId().equals(claims.tokenId())
                || !safeEquals(session.id(), claims.sessionId())
                || !safeEquals(session.tenantId(), claims.tenantId())
                || !safeEquals(metadata.subject(), claims.subject())
                || !safeEquals(session.channel(), claims.channel())
                || !safeEquals(metadata.provider(), claims.provider())) {
            return auditEventPort.recordSessionValidationFailed(
                            claims.tokenId().value(),
                            "session_claims_mismatch"
                    )
                    .then(Mono.error(new AuthenticationFailedException("Session claims mismatch")));
        }

        if (session.isRevoked()) {
            return auditEventPort.recordSessionValidationFailed(session.tokenId().value(), "session_revoked")
                    .then(Mono.error(new AuthenticationFailedException("Session revoked")));
        }

        if (session.isExpired(clockPort.now())) {
            return auditEventPort.recordSessionValidationFailed(session.tokenId().value(), "session_expired")
                    .then(Mono.error(new AuthenticationFailedException("Session expired")));
        }

        return sessionCachePort.isSessionBlacklisted(session.id())
                .defaultIfEmpty(false)
                .flatMap(blacklisted -> {
                    if (Boolean.TRUE.equals(blacklisted)) {
                        return auditEventPort.recordSessionValidationFailed(
                                        session.tokenId().value(),
                                        "session_blacklisted"
                                )
                                .then(Mono.error(new AuthenticationFailedException("Session revoked")));
                    }
                    return Mono.just(session);
                });
    }

    private AuthContext toAuthContext(IdentitySession session, JwtSessionClaims claims) {
        SessionMetadata metadata = readMetadata(session.metadataJson(), claims.subject(), claims.provider());

        return AuthContext.builder()
                .tenantId(session.tenantId())
                .subject(metadata.subject())
                .channel(normalizeChannel(session.channel()))
                .provider(metadata.provider())
                .authenticated(true)
                .authorities(metadata.authorities() != null ? metadata.authorities() : List.of())
                .attributes(metadata.attributes() != null ? metadata.attributes() : Map.of())
                .build();
    }

    private SessionMetadata readMetadata(String metadataJson, String fallbackSubject, String fallbackProvider) {
        if (metadataJson == null || metadataJson.isBlank()) {
            return new SessionMetadata(
                    fallbackSubject,
                    fallbackProvider,
                    List.of(),
                    Map.of()
            );
        }

        try {
            Map<String, Object> root = objectMapper.readValue(
                    metadataJson,
                    new TypeReference<Map<String, Object>>() {}
            );

            String subject = readString(root, "subject");
            String provider = readString(root, "provider");
            List<String> authorities = readStringList(root.get("authorities"));
            Map<String, Object> attributes = readObjectMap(root.get("attributes"));

            return new SessionMetadata(subject, provider, authorities, attributes);
        } catch (Exception ex) {
            throw new AuthenticationFailedException("Failed to read session metadata", ex);
        }
    }

    private String readString(Map<String, Object> source, String key) {
        Object value = source.get(key);
        return value == null ? null : String.valueOf(value);
    }

    @SuppressWarnings("unchecked")
    private List<String> readStringList(Object value) {
        if (value == null) {
            return List.of();
        }
        if (value instanceof List<?> list) {
            return list.stream()
                    .map(String::valueOf)
                    .toList();
        }
        return List.of();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readObjectMap(Object value) {
        if (value == null) {
            return Map.of();
        }
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }

    private String normalizeChannel(String channel) {
        return channel == null ? null : channel.toLowerCase();
    }

    private boolean safeEquals(String left, String right) {
        return Objects.equals(left, right);
    }

    private record SessionMetadata(
            String subject,
            String provider,
            List<String> authorities,
            Map<String, Object> attributes
    ) {
    }
}
