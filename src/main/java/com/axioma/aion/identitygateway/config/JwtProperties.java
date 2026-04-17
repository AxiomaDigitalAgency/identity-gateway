package com.axioma.aion.identitygateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.security.session-jwt")
public class JwtProperties {

    private String issuer;
    private String secretKey;
    private long ttlSeconds = 900;
}