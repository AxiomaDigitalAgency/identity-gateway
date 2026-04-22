package com.axioma.aion.identitygateway.adapter.out.persistence;


import com.axioma.aion.identitygateway.adapter.out.persistence.entity.IdentityCredentialEntity;
import com.axioma.aion.identitygateway.adapter.out.repository.IdentityContextRepository;
import com.axioma.aion.identitygateway.adapter.out.repository.IdentityCredentialRepository;
import com.axioma.aion.identitygateway.domain.model.IdentityCredential;
import com.axioma.aion.identitygateway.domain.port.out.CredentialPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CredentialPersistenceAdapter implements CredentialPort {

    private final IdentityCredentialRepository repository;
    private final IdentityContextRepository contextRepository;

    @Override
    public Mono<IdentityCredential> findById(UUID credentialId) {
        return repository.findById(credentialId)
                .flatMap(this::mapWithContext);
    }

    @Override
    public Mono<IdentityCredential> findByCredentialKey(String credentialKey) {
        return repository.findByCredentialKey(credentialKey)
                .flatMap(this::mapWithContext);
    }

    @Override
    public Mono<IdentityCredential> findByClientId(String clientId) {
        // si no lo usas aún, puedes dejarlo como:
        return Mono.empty();
    }

    private Mono<IdentityCredential> mapWithContext(
            IdentityCredentialEntity entity) {
        return contextRepository.findById(entity.getIdentityContextId())
                .map(ctx -> new IdentityCredential(
                        entity.getId(),
                        entity.getIdentityContextId(),
                        ctx.getTenantId(),
                        entity.getCredentialType(),
                        entity.getCredentialKey(),
                        entity.getSecretHash(),
                        entity.getStatus(),
                        entity.getEnabled()
                ));
    }
}