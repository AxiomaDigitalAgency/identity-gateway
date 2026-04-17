package com.axioma.aion.identitygateway.domain.port.out;

import com.axioma.aion.identitygateway.domain.model.OAuthTokenValidationResult;
import reactor.core.publisher.Mono;

public interface OAuthTokenValidationPort {

    Mono<OAuthTokenValidationResult> validate(String bearerToken);
}