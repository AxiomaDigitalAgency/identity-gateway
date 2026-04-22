package com.axioma.aion.identitygateway.adapter.out.repository;

import com.axioma.aion.identitygateway.adapter.out.persistence.entity.IdentityContextEntity;
import com.axioma.aion.identitygateway.adapter.out.persistence.entity.IdentityCredentialEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface IdentityContextRepository extends ReactiveCrudRepository<IdentityContextEntity, UUID> {
    Mono<IdentityContextEntity> findById(UUID id);
}
