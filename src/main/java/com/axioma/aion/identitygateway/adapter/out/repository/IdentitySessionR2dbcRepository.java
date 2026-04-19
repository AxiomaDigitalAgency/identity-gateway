package com.axioma.aion.identitygateway.adapter.out.persistence.repository;

import com.axioma.aion.identitygateway.adapter.out.persistence.entity.IdentitySessionEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

public interface IdentitySessionR2dbcRepository extends ReactiveCrudRepository<IdentitySessionEntity, String> {

    Mono<IdentitySessionEntity> findBySessionTokenId(String tokenId);

    @Modifying
    @Query("""
        UPDATE identity_session
           SET status = 'REVOKED',
               update_date = :revokedAt
         WHERE id = :sessionId
        """)
    Mono<Void> markSessionAsRevoked(String sessionId, OffsetDateTime revokedAt);
}
