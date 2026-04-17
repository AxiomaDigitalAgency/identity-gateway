package com.axioma.aion.identitygateway.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        SessionProperties.class,
        JwtProperties.class
})
public class SecurityConfig {
}