package com.axioma.aion.identitygateway.domain.port.out;

public interface IdGeneratorPort {

    String generateSessionId();

    String generateTokenId();
}