package com.axioma.aion.identitygateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.security.session")
public class SessionProperties {

    private long ttlSeconds = 900;
    private boolean blacklistEnabled = true;
}