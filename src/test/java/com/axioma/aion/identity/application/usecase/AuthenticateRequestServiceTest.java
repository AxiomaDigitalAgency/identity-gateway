package com.axioma.aion.identity.application.usecase;

import com.axioma.aion.identity.application.service.SecurityProviderResolver;
import com.axioma.aion.identity.domain.model.AuthenticationResult;
import com.axioma.aion.identity.domain.model.SecurityRequest;
import com.axioma.aion.identity.domain.port.out.SecurityProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class AuthenticateRequestServiceTest {

    @Test
    void should_delegate_authentication_to_resolved_provider() {
        SecurityProviderResolver resolver = mock(SecurityProviderResolver.class);
        SecurityProvider provider = mock(SecurityProvider.class);

        SecurityRequest request = SecurityRequest.builder()
                .channel("web")
                .build();

        AuthenticationResult expected = AuthenticationResult.builder()
                .success(false)
                .errorCode("NOT_IMPLEMENTED")
                .errorMessage("stub")
                .build();

        when(resolver.resolve()).thenReturn(provider);
        when(provider.authenticate(request)).thenReturn(expected);

        AuthenticateRequestService service = new AuthenticateRequestService(resolver);

        AuthenticationResult result = service.authenticate(request);

        assertSame(expected, result);
        verify(resolver).resolve();
        verify(provider).authenticate(request);
    }
}