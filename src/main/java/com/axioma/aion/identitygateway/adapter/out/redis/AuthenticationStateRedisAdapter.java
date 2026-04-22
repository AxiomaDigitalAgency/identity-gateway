package com.axioma.aion.identitygateway.adapter.out.redis;


import com.axioma.aion.identitygateway.domain.model.AuthenticatedPrincipal;
import com.axioma.aion.identitygateway.domain.port.out.AuthenticationStatePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationStateRedisAdapter implements AuthenticationStatePort {

    private final ReactiveRedisTemplate<String, AuthenticatedPrincipal> redisTemplate;
    private final Duration ttl = Duration.ofMinutes(2);

    private String key(UUID authenticationId) {
        return "auth:" + authenticationId;
    }

    @Override
    public Mono<Void> save(AuthenticatedPrincipal principal) {
        String key = key(principal.authenticationId());
        log.info("authentication_state_save_start redisKey={} authenticationId={} ttlSeconds={}",
                key, principal.authenticationId(), ttl.getSeconds());

        return redisTemplate.opsForValue()
                .set(key, principal, ttl)
                .doOnNext(saved -> log.info(
                        "authentication_state_save_complete redisKey={} authenticationId={} saved={}",
                        key, principal.authenticationId(), saved))
                .then();
    }

    @Override
    public Mono<AuthenticatedPrincipal> findByAuthenticationId(UUID authenticationId) {
        String key = key(authenticationId);
        return redisTemplate.opsForValue().get(key)
                .doOnNext(principal -> log.info(
                        "authentication_state_found redisKey={} authenticationId={}",
                        key, authenticationId))
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("authentication_state_not_found redisKey={} authenticationId={}", key, authenticationId);
                    return Mono.empty();
                }));
    }

    @Override
    public Mono<Void> markConsumed(UUID authenticationId) {
        String key = key(authenticationId);
        return redisTemplate.delete(key)
                .doOnNext(deleted -> log.info(
                        "authentication_state_mark_consumed redisKey={} authenticationId={} deletedCount={}",
                        key, authenticationId, deleted))
                .then();
    }

    @Override
    public Mono<Boolean> isConsumed(UUID authenticationId) {
        String key = key(authenticationId);
        return redisTemplate.hasKey(key)
                .map(exists -> !exists);
    }

    @Override
    public Mono<Void> delete(UUID authenticationId) {
        String key = key(authenticationId);
        return redisTemplate.delete(key)
                .doOnNext(deleted -> log.info(
                        "authentication_state_delete redisKey={} authenticationId={} deletedCount={}",
                        key, authenticationId, deleted))
                .then();
    }
}
