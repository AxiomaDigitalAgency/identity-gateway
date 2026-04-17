package com.axioma.aion.identitygateway.application.service;

import com.axioma.aion.identitygateway.application.command.RevokeSessionCommand;
import com.axioma.aion.identitygateway.domain.model.IdentitySession;
import com.axioma.aion.identitygateway.domain.model.valueobject.TokenId;
import com.axioma.aion.identitygateway.domain.port.out.*;
import com.axioma.aion.securitycore.exception.AuthenticationFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RevokeSessionServiceTest {

    @Mock
    private IdentitySessionRepositoryPort repository;

    @Mock
    private SessionCachePort cache;

    @Mock
    private ClockPort clock;

    @Mock
    private AuditEventPort audit;

    private RevokeSessionService service;

    @BeforeEach
    void setUp() {
        service = new RevokeSessionService(repository, cache, clock, audit);
    }

    @Test
    void shouldRevokeActiveSessionSuccessfully() {
        TokenId tokenId = new TokenId("tok-123");

        IdentitySession session = IdentitySession.builder()
                .sessionId("sess-123")
                .tenantId("tenant-1")
                .subject("user-123")
                .tokenId(tokenId)
                .issuedAt(OffsetDateTime.parse("2026-04-17T10:00:00Z"))
                .expiresAt(OffsetDateTime.parse("2026-04-17T10:15:00Z"))
                .revokedAt(null)
                .authorities("[]")
                .attributes("{}")
                .build();

        when(repository.findBySessionTokenId(tokenId)).thenReturn(Mono.just(session));
        when(clock.now()).thenReturn(OffsetDateTime.parse("2026-04-17T10:05:00Z"));

        when(repository.markSessionAsRevoked(any(), any()))
                .thenReturn(Mono.empty());

        when(cache.delete(tokenId)).thenReturn(Mono.empty());
        when(cache.blacklistSession(eq("sess-123"), any(Duration.class)))
                .thenReturn(Mono.empty());

        when(audit.recordSessionRevoked(any(), any(), any(), any(), any()))
                .thenReturn(Mono.empty());

        RevokeSessionCommand command = RevokeSessionCommand.builder()
                .tokenId(tokenId)
                .reason("logout")
                .requestedBy("user")
                .build();

        StepVerifier.create(service.execute(command))
                .assertNext(result -> {
                    assert result.sessionId().equals("sess-123");
                    assert result.revoked();
                })
                .verifyComplete();

        verify(repository).markSessionAsRevoked(eq("sess-123"), any());
        verify(cache).delete(tokenId);
        verify(cache).blacklistSession(eq("sess-123"), any(Duration.class));
        verify(audit).recordSessionRevoked(
                eq("tenant-1"),
                eq("sess-123"),
                eq("tok-123"),
                eq("logout"),
                eq("user")
        );
    }

    @Test
    void shouldReturnErrorWhenSessionNotFound() {
        TokenId tokenId = new TokenId("tok-404");

        when(repository.findBySessionTokenId(tokenId)).thenReturn(Mono.empty());

        RevokeSessionCommand command = RevokeSessionCommand.builder()
                .tokenId(tokenId)
                .reason("logout")
                .requestedBy("user")
                .build();

        StepVerifier.create(service.execute(command))
                .expectError(AuthenticationFailedException.class)
                .verify();
    }

    @Test
    void shouldBeIdempotentWhenSessionAlreadyRevoked() {
        TokenId tokenId = new TokenId("tok-123");

        IdentitySession session = IdentitySession.builder()
                .sessionId("sess-123")
                .tenantId("tenant-1")
                .subject("user-123")
                .tokenId(tokenId)
                .issuedAt(OffsetDateTime.parse("2026-04-17T10:00:00Z"))
                .expiresAt(OffsetDateTime.parse("2026-04-17T10:15:00Z"))
                .revokedAt(OffsetDateTime.parse("2026-04-17T10:02:00Z"))
                .authorities("[]")
                .attributes("{}")
                .build();

        when(repository.findBySessionTokenId(tokenId)).thenReturn(Mono.just(session));

        RevokeSessionCommand command = RevokeSessionCommand.builder()
                .tokenId(tokenId)
                .reason("logout")
                .requestedBy("user")
                .build();

        StepVerifier.create(service.execute(command))
                .assertNext(result -> {
                    assert result.revoked();
                })
                .verifyComplete();

        verify(repository, never()).markSessionAsRevoked(any(), any());
        verify(cache, never()).delete(any());
        verify(cache, never()).blacklistSession(any(), any());
    }
}