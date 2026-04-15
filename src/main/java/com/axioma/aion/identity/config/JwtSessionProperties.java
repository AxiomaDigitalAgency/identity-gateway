package com.axioma.aion.identity.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.security.session-jwt")
public class JwtSessionProperties {

    @NotBlank
    private String issuer;

    @NotBlank
    private String secretKey;

    @Min(60)
    private long ttlSeconds;
}