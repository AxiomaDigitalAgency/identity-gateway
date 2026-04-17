package com.axioma.aion.identitygateway.application.result;

import com.axioma.aion.identitygateway.domain.model.valueobject.TokenId;
import lombok.Builder;

@Builder
public record RevokeSessionResult(
        String sessionId,
        TokenId tokenId,
        boolean revoked
) {
}