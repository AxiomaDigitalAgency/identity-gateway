package com.axioma.aion.identitygateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.security")
public class SecurityRoutingProperties {

    private Map<String, String> providerRouting = new HashMap<>();
}