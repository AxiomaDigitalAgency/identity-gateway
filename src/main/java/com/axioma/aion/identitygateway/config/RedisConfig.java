package com.axioma.aion.identitygateway.config;

import com.axioma.aion.identitygateway.adapter.out.redis.entity.IdentitySessionRedisEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public ReactiveRedisTemplate<String, IdentitySessionRedisEntity> identitySessionRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory,
            ObjectMapper objectMapper
    ) {
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<IdentitySessionRedisEntity> valueSerializer =
                new Jackson2JsonRedisSerializer<>(IdentitySessionRedisEntity.class);
        valueSerializer.setObjectMapper(objectMapper);

        RedisSerializationContext<String, IdentitySessionRedisEntity> serializationContext =
                RedisSerializationContext.<String, IdentitySessionRedisEntity>newSerializationContext(keySerializer)
                        .value(valueSerializer)
                        .hashKey(keySerializer)
                        .hashValue(valueSerializer)
                        .build();

        return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
    }
}
