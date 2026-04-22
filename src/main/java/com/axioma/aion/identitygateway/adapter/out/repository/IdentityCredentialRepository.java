package com.axioma.aion.identitygateway.adapter.out.repository;

import com.axioma.aion.identitygateway.adapter.out.persistence.entity.IdentityCredentialEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface IdentityCredentialRepository extends ReactiveCrudRepository<IdentityCredentialEntity, UUID> {
    Mono<IdentityCredentialEntity> findByCredentialKey(String credentialKey);
    Mono<IdentityCredentialEntity> findByCredentialKeyIgnoreCase(String credentialKey);
    Mono<IdentityCredentialEntity> findById(UUID id);
}