package com.axioma.aion.identitygateway.domain.model.valueobject;

public record TokenId(String value) {

    public TokenId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("tokenId must not be blank");
        }
    }
}