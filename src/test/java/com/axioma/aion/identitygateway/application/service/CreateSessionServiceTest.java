package com.axioma.aion.identitygateway.application.service;

import com.axioma.aion.identitygateway.application.command.CreateSessionCommand;
import com.axioma.aion.identitygateway.application.result.CreateSessionResult;
import com.axioma.aion.identitygateway.config.SessionProperties;
import com.axioma.aion.identitygateway.domain.model.IdentitySession;
import com.axioma.aion.identitygateway.domain.model.valueobject.TokenId;
import com.axioma.aion.identitygateway.domain.port.out.*;
import com.axioma.aion.securitycore.exception.InvalidSecurityRequestException;
import com.axioma.aion.securitycore.model.AuthContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateSessionServiceTest {

    @Mock
    private IdentitySessionRepositoryPort repository;

    @Mock
    private SessionCachePort cache;

    @Mock
    private JwtSessionProviderPort jwtProvider;

    @Mock
    private ClockPort clock;

    @Mock
    private IdGeneratorPort idGenerator;

    @Mock
    private AuditEventPort audit;

    private SessionProperties sessionProperties;

    private CreateSessionService service;

    @BeforeEach
    void setUp() {
        sessionProperties = new SessionProperties();
        sessionProperties.setTtlSeconds(900);

        service = new CreateSessionService(
                repository,
                cache,
                jwtProvider,
                clock,
                idGenerator,
                audit,
                sessionProperties
        );
    }

    @Test
    void shouldCreateSessionSuccessfully() {
        // given
        OffsetDateTime now = OffsetDateTime.parse("2026-04-17T10:00:00Z");

        AuthContext authContext = AuthContext.builder()
                .tenantId("tenant-1")
                .subject("user-123")
                .channel("web")
                .provider("widget")
                .authenticated(true)
                .authorities(List.of())
                .attributes(Map.of())
                .build();

        CreateSessionCommand command = CreateSessionCommand.builder()
                .authContext(authContext)
                .build();

        when(clock.now()).thenReturn(now);
        when(idGenerator.generateSessionId()).thenReturn("sess-123");
        when(idGenerator.generateTokenId()).thenReturn("tok-123");

        when(repository.save(any()))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        when(jwtProvider.generate(any()))
                .thenReturn(Mono.just("jwt-token"));

        when(cache.save(any(), any()))
                .thenReturn(Mono.empty());

        when(audit.recordSessionCreated(any(), any(), any(), any(), any(), any()))
                .thenReturn(Mono.empty());

        // when
        Mono<CreateSessionResult> result = service.execute(command);

        // then
        StepVerifier.create(result)
                .assertNext(res -> {
                    assert res.sessionToken().equals("jwt-token");
                    assert res.sessionId().equals("sess-123");
                    assert res.tokenId().equals(new TokenId("tok-123"));
                    assert res.authContext().tenantId().equals("tenant-1");
                })
                .verifyComplete();

        verify(repository).save(any(IdentitySession.class));
        verify(jwtProvider).generate(any());
        verify(cache).save(any(), any(Duration.class));
        verify(audit).recordSessionCreated(
                eq("tenant-1"),
                eq("sess-123"),
                eq("tok-123"),
                eq("user-123"),
                eq("web"),
                eq("widget")
        );
    }

    @Test
    void shouldFailWhenAuthContextIsInvalid() {
        // given
        AuthContext invalidContext = AuthContext.builder()
                .tenantId(null)
                .subject(null)
                .channel(null)
                .provider(null)
                .authenticated(false)
                .build();

        CreateSessionCommand command = CreateSessionCommand.builder()
                .authContext(invalidContext)
                .build();

        // when
        Mono<CreateSessionResult> result = service.execute(command);

        // then
        StepVerifier.create(result)
                .expectError(InvalidSecurityRequestException.class)
                .verify();

        verifyNoInteractions(repository, jwtProvider, cache, audit);
    }

    @Test
    void shouldFailWhenAuthContextIsNull() {
        // given
        CreateSessionCommand command = CreateSessionCommand.builder()
                .authContext(null)
                .build();

        // when
        Mono<CreateSessionResult> result = service.execute(command);

        // then
        StepVerifier.create(result)
                .expectError(InvalidSecurityRequestException.class)
                .verify();

        verifyNoInteractions(repository, jwtProvider, cache, audit);
    }
}