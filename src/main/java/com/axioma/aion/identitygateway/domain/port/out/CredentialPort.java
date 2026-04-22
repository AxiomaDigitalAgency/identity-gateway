package com.axioma.aion.identitygateway.domain.port.out;

import com.axioma.aion.identitygateway.domain.model.IdentityCredential;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CredentialPort {
    Mono<IdentityCredential> findById(UUID credentialId);
    Mono<IdentityCredential> findByCredentialKey(String credentialKey);
    Mono<IdentityCredential> findByClientId(String clientId);
}