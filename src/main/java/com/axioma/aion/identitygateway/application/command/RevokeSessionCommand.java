package com.axioma.aion.identitygateway.application.command;

import com.axioma.aion.identitygateway.domain.model.valueobject.TokenId;
import lombok.Builder;

@Builder
public record RevokeSessionCommand(
        TokenId tokenId,
        String reason,
        String requestedBy
) {
}