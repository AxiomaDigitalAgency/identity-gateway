package com.axioma.aion.identitygateway.adapter.out.persistence;

import com.axioma.aion.identitygateway.adapter.out.persistence.entity.IdentitySessionEntity;
import com.axioma.aion.identitygateway.adapter.out.persistence.mapper.IdentityPersistenceMapper;
import com.axioma.aion.identitygateway.adapter.out.persistence.repository.IdentitySessionR2dbcRepository;
import com.axioma.aion.identitygateway.domain.model.IdentitySession;
import com.axioma.aion.identitygateway.domain.model.valueobject.TokenId;
import com.axioma.aion.identitygateway.domain.port.out.IdentitySessionRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class IdentitySessionRepositoryAdapter implements IdentitySessionRepositoryPort {

    private final IdentitySessionR2dbcRepository repository;
    private final IdentityPersistenceMapper mapper;
    private final R2dbcEntityTemplate entityTemplate;

    @Override
    public Mono<IdentitySession> findBySessionTokenId(TokenId tokenId) {
        return repository.findBySessionTokenId(tokenId.value())
                .map(mapper::toDomain)
                .doOnSuccess(session -> {
                    if (session != null) {
                        log.debug("identity_session_found tokenId={} sessionId={} tenantId={}",
                                tokenId.value(), session.id(), session.tenantId());
                    } else {
                        log.debug("identity_session_not_found tokenId={}", tokenId.value());
                    }
                })
                .doOnError(error -> log.error("identity_session_find_error tokenId={} message={}",
                        tokenId.value(), error.getMessage(), error));
    }

    @Override
    public Mono<IdentitySession> save(IdentitySession identitySession) {
        IdentitySessionEntity entity = mapper.toEntity(identitySession);
        return entityTemplate.insert(IdentitySessionEntity.class)
                .using(entity)
                .doOnSuccess(saved -> log.info(
                        "identity_session_save_success tenantId={} channel={}",
                        saved.getTenantId(),
                        saved.getChannel()
                ))
                .doOnError(error -> log.error(
                        "identity_session_save_error tenantId={} channel={} message={}",
                        entity.getTenantId(),
                        entity.getChannel(),
                        error.getMessage(),
                        error
                ))
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Void> markSessionAsRevoked(String sessionId, OffsetDateTime revokedAt) {
        return repository.markSessionAsRevoked(sessionId, revokedAt);
    }
}
