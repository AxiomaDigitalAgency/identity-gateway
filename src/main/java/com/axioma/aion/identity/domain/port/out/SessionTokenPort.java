package com.axioma.aion.identity.domain.port.out;

import com.axioma.aion.identity.domain.model.SessionValidationResult;

public interface SessionTokenPort {
    SessionValidationResult validate(String sessionToken, String channel);
}