package com.axioma.aion.identitygateway.domain.port.out;

import com.axioma.aion.securitycore.port.SecurityProvider;

public interface SecurityProviderResolver {

    SecurityProvider resolve(String channel, String authType);
}