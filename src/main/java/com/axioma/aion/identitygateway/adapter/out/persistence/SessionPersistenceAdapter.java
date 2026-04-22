package com.axioma.aion.identitygateway.adapter.out.persistence;

import com.axioma.aion.identitygateway.domain.model.IdentitySession;
import com.axioma.aion.identitygateway.domain.port.out.SessionPort;
import com.axioma.aion.securitycore.model.AuthenticationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class SessionPersistenceAdapter implements SessionPort {

    private final DatabaseClient databaseClient;

    @Override
    public Mono<IdentitySession> save(IdentitySession session) {
        OffsetDateTime now = OffsetDateTime.now();
        log.info("session_persistence_save_start sessionId={} tenantId={} credentialId={} status={} expiresAt={}",
                session.id(), session.tenantId(), session.credentialId(), session.status(), session.expiresAt());

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql("""
                INSERT INTO identity_session (
                    id,
                    tenant_id,
                    credential_id,
                    subject,
                    channel,
                    authentication_type,
                    status,
                    authenticated_at,
                    session_created_at,
                    expires_at,
                    revoked_at,
                    create_date,
                    update_date,
                    active_reg_ind
                )
                VALUES (
                    :id,
                    :tenantId,
                    :credentialId,
                    :subject,
                    :channel,
                    :authenticationType,
                    :status,
                    :authenticatedAt,
                    :sessionCreatedAt,
                    :expiresAt,
                    :revokedAt,
                    :createDate,
                    :updateDate,
                    :activeRegInd
                )
                """)
                .bind("id", session.id())
                .bind("tenantId", session.tenantId())
                .bind("credentialId", session.credentialId())
                .bind("subject", session.subject())
                .bind("channel", session.channel())
                .bind("authenticationType", session.authenticationType().name())
                .bind("status", session.status())
                .bind("authenticatedAt", session.authenticatedAt())
                .bind("sessionCreatedAt", session.sessionCreatedAt())
                .bind("expiresAt", session.expiresAt())
                .bind("createDate", now)
                .bind("updateDate", now)
                .bind("activeRegInd", true);

        spec = session.revokedAt() != null
                ? spec.bind("revokedAt", session.revokedAt())
                : spec.bindNull("revokedAt", OffsetDateTime.class);

        return spec.fetch()
                .rowsUpdated()
                .doOnNext(rows -> log.info(
                        "session_persistence_save_rows_updated sessionId={} rowsUpdated={}",
                        session.id(),
                        rows))
                .flatMap(rows -> rows > 0
                        ? Mono.just(session)
                        : Mono.error(new IllegalStateException("Session could not be saved")))
                .doOnError(error -> log.error(
                        "session_persistence_save_error sessionId={} message={}",
                        session.id(),
                        error.getMessage(),
                        error));
    }

    @Override
    public Mono<IdentitySession> findById(UUID sessionId) {
        log.info("session_persistence_find_by_id_start sessionId={}", sessionId);
        return databaseClient.sql("""
                SELECT
                    id,
                    tenant_id,
                    credential_id,
                    subject,
                    channel,
                    authentication_type,
                    status,
                    authenticated_at,
                    session_created_at,
                    expires_at,
                    revoked_at
                FROM identity_session
                WHERE id = :id
                """)
                .bind("id", sessionId)
                .map((row, metadata) -> new IdentitySession(
                        row.get("id", UUID.class),
                        row.get("tenant_id", UUID.class),
                        row.get("credential_id", UUID.class),
                        row.get("subject", String.class),
                        row.get("channel", String.class),
                        AuthenticationType.valueOf(row.get("authentication_type", String.class)),
                        row.get("status", String.class),
                        row.get("authenticated_at", OffsetDateTime.class),
                        row.get("session_created_at", OffsetDateTime.class),
                        row.get("expires_at", OffsetDateTime.class),
                        row.get("revoked_at", OffsetDateTime.class)
                ))
                .one()
                .doOnNext(session -> log.info(
                        "session_persistence_find_by_id_found sessionId={} tenantId={} status={}",
                        sessionId,
                        session.tenantId(),
                        session.status()))
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("session_persistence_find_by_id_not_found sessionId={}", sessionId);
                    return Mono.empty();
                }))
                .doOnError(error -> log.error(
                        "session_persistence_find_by_id_error sessionId={} message={}",
                        sessionId,
                        error.getMessage(),
                        error));
    }

    @Override
    public Mono<Void> revoke(UUID sessionId) {
        OffsetDateTime revokedAt = OffsetDateTime.now();
        log.info("session_persistence_revoke_start sessionId={} revokedAt={}", sessionId, revokedAt);

        return databaseClient.sql("""
                UPDATE identity_session
                SET status = 'REVOKED',
                    revoked_at = :revokedAt,
                    update_date = :revokedAt
                WHERE id = :id
                """)
                .bind("id", sessionId)
                .bind("revokedAt", revokedAt)
                .fetch()
                .rowsUpdated()
                .doOnNext(rows -> log.info(
                        "session_persistence_revoke_rows_updated sessionId={} rowsUpdated={}",
                        sessionId,
                        rows))
                .doOnError(error -> log.error(
                        "session_persistence_revoke_error sessionId={} message={}",
                        sessionId,
                        error.getMessage(),
                        error))
                .then();
    }
}
