package com.axioma.aion.identity.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
@RequiredArgsConstructor
public class JwtSessionConfig {

    private final JwtSessionProperties properties;

    @Bean
    public SecretKey sessionJwtSecretKey() {
        byte[] keyBytes = properties.getSecretKey().getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    @Bean
    public JwtEncoder sessionJwtEncoder(SecretKey sessionJwtSecretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(sessionJwtSecretKey));
    }

    @Bean
    public JwtDecoder sessionJwtDecoder(SecretKey sessionJwtSecretKey) {
        return NimbusJwtDecoder.withSecretKey(sessionJwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }
}