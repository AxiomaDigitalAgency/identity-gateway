package com.axioma.aion.identitygateway.config;

import com.axioma.aion.identitygateway.domain.model.AuthenticatedPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public ReactiveRedisTemplate<String, AuthenticatedPrincipal> authRedisTemplate(
            ReactiveRedisConnectionFactory factory,
            ObjectMapper objectMapper
    ) {
        Jackson2JsonRedisSerializer<AuthenticatedPrincipal> serializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, AuthenticatedPrincipal.class);

        RedisSerializationContext<String, AuthenticatedPrincipal> context =
                RedisSerializationContext.<String, AuthenticatedPrincipal>newSerializationContext(RedisSerializer.string())
                        .value(serializer)
                        .hashKey(RedisSerializer.string())
                        .hashValue(serializer)
                        .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }
}
