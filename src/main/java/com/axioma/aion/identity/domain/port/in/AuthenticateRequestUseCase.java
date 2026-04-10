package com.axioma.aion.identity.domain.port.in;

import com.axioma.aion.identity.domain.model.AuthenticationResult;
import com.axioma.aion.identity.domain.model.SecurityRequest;

public interface AuthenticateRequestUseCase {
    AuthenticationResult authenticate(SecurityRequest request);
}