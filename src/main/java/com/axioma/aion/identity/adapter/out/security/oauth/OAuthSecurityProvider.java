package com.axioma.aion.identity.adapter.out.security.oauth;

import com.axioma.aion.identity.domain.model.AuthenticationResult;
import com.axioma.aion.identity.domain.model.SecurityMode;
import com.axioma.aion.identity.domain.model.SecurityRequest;
import com.axioma.aion.identity.domain.port.out.SecurityProvider;
import org.springframework.stereotype.Component;

@Component
public class OAuthSecurityProvider implements SecurityProvider {

    @Override
    public SecurityMode supports() {
        return SecurityMode.OAUTH;
    }

    @Override
    public AuthenticationResult authenticate(SecurityRequest request) {
        return AuthenticationResult.builder()
                .success(false)
                .errorCode("NOT_IMPLEMENTED")
                .errorMessage("OAuth security provider not implemented yet")
                .build();
    }
}