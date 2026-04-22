package com.axioma.aion.identitygateway.adapter.out.repository;

import com.axioma.aion.identitygateway.adapter.out.persistence.entity.IdentityOriginRuleEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface IdentityOriginRuleRepository extends ReactiveCrudRepository<IdentityOriginRuleEntity, UUID> {

    Flux<IdentityOriginRuleEntity> findByIdentityCredentialId(UUID credentialId);
}