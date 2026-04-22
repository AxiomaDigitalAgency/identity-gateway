package com.axioma.aion.identitygateway.domain.port.out;

import java.util.UUID;

public interface IdGeneratorPort {
    UUID generate();
}