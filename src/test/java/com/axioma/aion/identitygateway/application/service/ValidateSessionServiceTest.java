package com.axioma.aion.identitygateway.application.service;

import com.axioma.aion.identitygateway.application.command.ValidateSessionCommand;
import com.axioma.aion.identitygateway.domain.model.IdentitySession;
import com.axioma.aion.identitygateway.domain.model.JwtSessionClaims;
import com.axioma.aion.identitygateway.domain.model.valueobject.TokenId;
import com.axioma.aion.identitygateway.domain.port.out.AuditEventPort;
import com.axioma.aion.identitygateway.domain.port.out.ClockPort;
import com.axioma.aion.identitygateway.domain.port.out.IdentitySessionRepositoryPort;
import com.axioma.aion.identitygateway.domain.port.out.JwtSessionProviderPort;
import com.axioma.aion.identitygateway.domain.port.out.SessionCachePort;
import com.axioma.aion.securitycore.exception.AuthenticationFailedException;
import com.axioma.aion.securitycore.exception.InvalidSecurityRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidateSessionServiceTest {

    @Mock
    private JwtSessionProviderPort jwtSessionProviderPort;

    @Mock
    private SessionCachePort sessionCachePort;

    @Mock
    private IdentitySessionRepositoryPort identitySessionRepositoryPort;

    @Mock
    private ClockPort clockPort;

    @Mock
    private AuditEventPort auditEventPort;

    private ValidateSessionService service;

    @BeforeEach
    void setUp() {
        service = new ValidateSessionService(
                jwtSessionProviderPort,
                sessionCachePort,
                identitySessionRepositoryPort,
                clockPort,
                auditEventPort
        );
    }

    @Test
    void shouldReturnErrorWhenSessionTokenIsBlank() {
        ValidateSessionCommand command = ValidateSessionCommand.builder()
                .sessionToken(" ")
                .build();

        StepVerifier.create(service.execute(command))
                .expectError(InvalidSecurityRequestException.class)
                .verify();

        verifyNoInteractions(jwtSessionProviderPort, sessionCachePort, identitySessionRepositoryPort, auditEventPort);
    }

    @Test
    void shouldValidateSessionFromCacheSuccessfully() {
        TokenId tokenId = new TokenId("tok-123");
        JwtSessionClaims claims = JwtSessionClaims.builder()
                .tokenId(tokenId)
                .sessionId("sess-123")
                .tenantId("tenant-1")
                .subject("user-123")
                .channel("web")
                .provider("widget")
                .build();

        IdentitySession session = IdentitySession.builder()
                .sessionId("sess-123")
                .tenantId("tenant-1")
                .subject("user-123")
                .channel("web")
                .provider("widget")
                .tokenId(tokenId)
                .issuedAt(OffsetDateTime.parse("2026-04-17T10:00:00Z"))
                .expiresAt(OffsetDateTime.parse("2026-04-17T10:15:00Z"))
                .revokedAt(null)
                .authorities("[]")
                .attributes("{}")
                .build();

        when(jwtSessionProviderPort.parse("jwt-token")).thenReturn(Mono.just(claims));
        when(sessionCachePort.findByTokenId(tokenId)).thenReturn(Mono.just(session));
        when(clockPort.now()).thenReturn(OffsetDateTime.parse("2026-04-17T10:05:00Z"));
        when(sessionCachePort.isSessionBlacklisted("sess-123")).thenReturn(Mono.just(false));

        StepVerifier.create(service.execute(
                        ValidateSessionCommand.builder().sessionToken("jwt-token").build()
                ))
                .assertNext(result -> {
                    assert result.authContext().tenantId().equals("tenant-1");
                    assert result.authContext().subject().equals("user-123");
                    assert result.authContext().channel().equals("web");
                    assert result.authContext().provider().equals("widget");
                })
                .verifyComplete();

        verify(sessionCachePort).findByTokenId(tokenId);
        verify(identitySessionRepositoryPort, never()).findBySessionTokenId(any());
    }

    @Test
    void shouldValidateSessionFromRepositoryWhenCacheMiss() {
        TokenId tokenId = new TokenId("tok-123");
        JwtSessionClaims claims = JwtSessionClaims.builder()
                .tokenId(tokenId)
                .sessionId("sess-123")
                .tenantId("tenant-1")
                .subject("user-123")
                .channel("web")
                .provider("widget")
                .build();

        IdentitySession session = IdentitySession.builder()
                .sessionId("sess-123")
                .tenantId("tenant-1")
                .subject("user-123")
                .channel("web")
                .provider("widget")
                .tokenId(tokenId)
                .issuedAt(OffsetDateTime.parse("2026-04-17T10:00:00Z"))
                .expiresAt(OffsetDateTime.parse("2026-04-17T10:15:00Z"))
                .revokedAt(null)
                .authorities("[]")
                .attributes("{}")
                .build();

        when(jwtSessionProviderPort.parse("jwt-token")).thenReturn(Mono.just(claims));
        when(sessionCachePort.findByTokenId(tokenId)).thenReturn(Mono.empty());
        when(identitySessionRepositoryPort.findBySessionTokenId(tokenId)).thenReturn(Mono.just(session));
        when(clockPort.now()).thenReturn(OffsetDateTime.parse("2026-04-17T10:05:00Z"));
        when(sessionCachePort.isSessionBlacklisted("sess-123")).thenReturn(Mono.just(false));

        StepVerifier.create(service.execute(
                        ValidateSessionCommand.builder().sessionToken("jwt-token").build()
                ))
                .assertNext(result -> {
                    assert result.authContext().tenantId().equals("tenant-1");
                    assert result.authContext().subject().equals("user-123");
                })
                .verifyComplete();

        verify(sessionCachePort).findByTokenId(tokenId);
        verify(identitySessionRepositoryPort).findBySessionTokenId(tokenId);
    }

    @Test
    void shouldReturnErrorWhenSessionNotFound() {
        TokenId tokenId = new TokenId("tok-404");
        JwtSessionClaims claims = JwtSessionClaims.builder()
                .tokenId(tokenId)
                .sessionId("sess-404")
                .tenantId("tenant-1")
                .subject("user-404")
                .channel("web")
                .provider("widget")
                .build();

        when(jwtSessionProviderPort.parse("jwt-token")).thenReturn(Mono.just(claims));
        when(sessionCachePort.findByTokenId(tokenId)).thenReturn(Mono.empty());
        when(identitySessionRepositoryPort.findBySessionTokenId(tokenId)).thenReturn(Mono.empty());

        StepVerifier.create(service.execute(
                        ValidateSessionCommand.builder().sessionToken("jwt-token").build()
                ))
                .expectError()
                .verify();

        verify(sessionCachePort).findByTokenId(tokenId);
        verify(identitySessionRepositoryPort).findBySessionTokenId(tokenId);
    }

    @Test
    void shouldReturnErrorWhenSessionIsRevoked() {
        TokenId tokenId = new TokenId("tok-123");
        JwtSessionClaims claims = JwtSessionClaims.builder()
                .tokenId(tokenId)
                .sessionId("sess-123")
                .tenantId("tenant-1")
                .subject("user-123")
                .channel("web")
                .provider("widget")
                .build();

        IdentitySession session = IdentitySession.builder()
                .sessionId("sess-123")
                .tenantId("tenant-1")
                .subject("user-123")
                .channel("web")
                .provider("widget")
                .tokenId(tokenId)
                .issuedAt(OffsetDateTime.parse("2026-04-17T10:00:00Z"))
                .expiresAt(OffsetDateTime.parse("2026-04-17T10:15:00Z"))
                .revokedAt(OffsetDateTime.parse("2026-04-17T10:02:00Z"))
                .authorities("[]")
                .attributes("{}")
                .build();

        when(jwtSessionProviderPort.parse("jwt-token")).thenReturn(Mono.just(claims));
        when(sessionCachePort.findByTokenId(tokenId)).thenReturn(Mono.just(session));
        when(auditEventPort.recordSessionValidationFailed(anyString(), anyString()))
                .thenReturn(Mono.empty());

        StepVerifier.create(service.execute(
                        ValidateSessionCommand.builder().sessionToken("jwt-token").build()
                ))
                .expectError(AuthenticationFailedException.class)
                .verify();

        verify(auditEventPort).recordSessionValidationFailed("tok-123", "session_revoked");
    }

    @Test
    void shouldReturnErrorWhenSessionIsExpired() {
        TokenId tokenId = new TokenId("tok-123");
        JwtSessionClaims claims = JwtSessionClaims.builder()
                .tokenId(tokenId)
                .sessionId("sess-123")
                .tenantId("tenant-1")
                .subject("user-123")
                .channel("web")
                .provider("widget")
                .build();

        IdentitySession session = IdentitySession.builder()
                .sessionId("sess-123")
                .tenantId("tenant-1")
                .subject("user-123")
                .channel("web")
                .provider("widget")
                .tokenId(tokenId)
                .issuedAt(OffsetDateTime.parse("2026-04-17T10:00:00Z"))
                .expiresAt(OffsetDateTime.parse("2026-04-17T10:01:00Z"))
                .revokedAt(null)
                .authorities("[]")
                .attributes("{}")
                .build();

        when(jwtSessionProviderPort.parse("jwt-token")).thenReturn(Mono.just(claims));
        when(sessionCachePort.findByTokenId(tokenId)).thenReturn(Mono.just(session));
        when(clockPort.now()).thenReturn(OffsetDateTime.parse("2026-04-17T10:05:00Z"));
        when(auditEventPort.recordSessionValidationFailed(anyString(), anyString()))
                .thenReturn(Mono.empty());

        StepVerifier.create(service.execute(
                        ValidateSessionCommand.builder().sessionToken("jwt-token").build()
                ))
                .expectError(AuthenticationFailedException.class)
                .verify();

        verify(auditEventPort).recordSessionValidationFailed("tok-123", "session_expired");
    }

    @Test
    void shouldReturnErrorWhenSessionIsBlacklisted() {
        TokenId tokenId = new TokenId("tok-123");
        JwtSessionClaims claims = JwtSessionClaims.builder()
                .tokenId(tokenId)
                .sessionId("sess-123")
                .tenantId("tenant-1")
                .subject("user-123")
                .channel("web")
                .provider("widget")
                .build();

        IdentitySession session = IdentitySession.builder()
                .sessionId("sess-123")
                .tenantId("tenant-1")
                .subject("user-123")
                .channel("web")
                .provider("widget")
                .tokenId(tokenId)
                .issuedAt(OffsetDateTime.parse("2026-04-17T10:00:00Z"))
                .expiresAt(OffsetDateTime.parse("2026-04-17T10:15:00Z"))
                .revokedAt(null)
                .authorities("[]")
                .attributes("{}")
                .build();

        when(jwtSessionProviderPort.parse("jwt-token")).thenReturn(Mono.just(claims));
        when(sessionCachePort.findByTokenId(tokenId)).thenReturn(Mono.just(session));
        when(clockPort.now()).thenReturn(OffsetDateTime.parse("2026-04-17T10:05:00Z"));
        when(sessionCachePort.isSessionBlacklisted("sess-123")).thenReturn(Mono.just(true));
        when(auditEventPort.recordSessionValidationFailed("tok-123", "session_blacklisted")).thenReturn(Mono.empty());

        StepVerifier.create(service.execute(
                        ValidateSessionCommand.builder().sessionToken("jwt-token").build()
                ))
                .expectError(AuthenticationFailedException.class)
                .verify();

        verify(auditEventPort).recordSessionValidationFailed("tok-123", "session_blacklisted");
    }
}