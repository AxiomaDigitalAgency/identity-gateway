package com.axioma.aion.identity.application.usecase;

import com.axioma.aion.identity.application.service.SecurityProviderResolver;
import com.axioma.aion.identity.domain.model.AuthenticationResult;
import com.axioma.aion.identity.domain.model.SecurityRequest;
import com.axioma.aion.identity.domain.port.in.AuthenticateRequestUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticateRequestService implements AuthenticateRequestUseCase {

    private final SecurityProviderResolver securityProviderResolver;

    @Override
    public AuthenticationResult authenticate(SecurityRequest request) {
        return securityProviderResolver.resolve().authenticate(request);
    }
}