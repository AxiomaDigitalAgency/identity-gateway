package com.axioma.aion.identitygateway.adapter.out.id;

import com.axioma.aion.identitygateway.domain.port.out.IdGeneratorPort;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UuidIdGeneratorAdapter implements IdGeneratorPort {

    @Override
    public String generateSessionId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public String generateTokenId() {
        return UUID.randomUUID().toString();
    }
}