package com.axioma.aion.identity.domain.port.out;

import com.axioma.aion.identity.domain.model.AuthenticationResult;
import com.axioma.aion.identity.domain.model.SecurityMode;
import com.axioma.aion.identity.domain.model.SecurityRequest;

public interface SecurityProvider {
    SecurityMode supports();
    AuthenticationResult authenticate(SecurityRequest request);
}