package com.axioma.aion.identitygateway.adapter.out.persistence;

import com.axioma.aion.identitygateway.adapter.out.persistence.entity.IdentitySessionEntity;
import com.axioma.aion.identitygateway.adapter.out.persistence.mapper.IdentityPersistenceMapper;
import com.axioma.aion.identitygateway.adapter.out.persistence.repository.IdentitySessionR2dbcRepository;
import com.axioma.aion.identitygateway.domain.model.IdentitySession;
import com.axioma.aion.identitygateway.domain.model.valueobject.TokenId;
import com.axioma.aion.identitygateway.domain.port.out.IdentitySessionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class IdentitySessionRepositoryAdapter implements IdentitySessionRepositoryPort {

    private final IdentitySessionR2dbcRepository repository;
    private final IdentityPersistenceMapper mapper;

    @Override
    public Mono<IdentitySession> findBySessionTokenId(TokenId tokenId) {
        return repository.findByTokenId(tokenId.value())
                .map(mapper::toDomain);
    }

    @Override
    public Mono<IdentitySession> save(IdentitySession identitySession) {
        IdentitySessionEntity entity = mapper.toEntity(identitySession);
        return repository.save(entity)
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Void> markSessionAsRevoked(String sessionId, OffsetDateTime revokedAt) {
        return repository.markSessionAsRevoked(sessionId, revokedAt);
    }
}