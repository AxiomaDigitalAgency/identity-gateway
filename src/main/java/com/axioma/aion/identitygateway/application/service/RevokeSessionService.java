package com.axioma.aion.identitygateway.application.service;

import com.axioma.aion.identitygateway.application.command.RevokeSessionCommand;
import com.axioma.aion.identitygateway.application.result.RevokeSessionResult;
import com.axioma.aion.identitygateway.domain.model.IdentitySession;
import com.axioma.aion.identitygateway.domain.port.out.SessionPort;
import com.axioma.aion.securitycore.exception.AuthenticationFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class RevokeSessionService {

    private final SessionPort sessionPort;

    public Mono<RevokeSessionResult> execute(RevokeSessionCommand command) {
        if (command.sessionId() == null) {
            return Mono.error(new IllegalArgumentException("sessionId is required"));
        }

        log.info("revoke_session_start sessionId={} reason={} requestedBy={}",
                command.sessionId(), command.reason(), command.requestedBy());

        return sessionPort.findById(command.sessionId())
                .switchIfEmpty(Mono.error(new AuthenticationFailedException("Session not found")))
                .flatMap(session -> revokeIfNeeded(command, session));
    }

    private Mono<RevokeSessionResult> revokeIfNeeded(RevokeSessionCommand command, IdentitySession session) {
        if (session.isRevoked()) {
            log.info("revoke_session_already_revoked sessionId={}", session.id());
            return Mono.just(buildResult(session, true));
        }

        return sessionPort.revoke(session.id())
                .doOnSuccess(unused -> log.info("revoke_session_completed sessionId={}", session.id()))
                .thenReturn(buildResult(session, true));
    }

    private RevokeSessionResult buildResult(IdentitySession session, boolean revoked) {
        return RevokeSessionResult.builder()
                .sessionId(session.id() != null ? session.id().toString() : null)
                .revoked(revoked)
                .build();
    }
}
