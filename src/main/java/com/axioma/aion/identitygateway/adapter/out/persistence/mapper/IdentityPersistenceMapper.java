package com.axioma.aion.identitygateway.adapter.out.persistence.mapper;

import com.axioma.aion.identitygateway.adapter.out.persistence.entity.IdentitySessionEntity;
import com.axioma.aion.identitygateway.domain.model.IdentitySession;
import com.axioma.aion.identitygateway.domain.model.valueobject.TokenId;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public abstract class IdentityPersistenceMapper {

    protected final ObjectMapper objectMapper = new ObjectMapper();

    @Mapping(target = "tokenId", expression = "java(mapTokenId(entity.getTokenId()))")
    public abstract IdentitySession toDomain(IdentitySessionEntity entity);

    @Mapping(target = "tokenId", expression = "java(writeTokenId(identitySession.tokenId()))")
    public abstract IdentitySessionEntity toEntity(IdentitySession identitySession);

    protected TokenId mapTokenId(String value) {
        return value == null ? null : new TokenId(value);
    }

    protected String writeTokenId(TokenId tokenId) {
        return tokenId == null ? null : tokenId.value();
    }

    protected List<String> readAuthorities(String authoritiesJson) {
        if (authoritiesJson == null || authoritiesJson.isBlank()) {
            return List.of();
        }

        try {
            return objectMapper.readValue(authoritiesJson, new TypeReference<>() {});
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to deserialize authoritiesJson", ex);
        }
    }

    protected String writeAuthorities(List<String> authorities) {
        try {
            return objectMapper.writeValueAsString(authorities != null ? authorities : List.of());
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize authorities", ex);
        }
    }

    protected Map<String, Object> readAttributes(String attributesJson) {
        if (attributesJson == null || attributesJson.isBlank()) {
            return Map.of();
        }

        try {
            return objectMapper.readValue(attributesJson, new TypeReference<>() {});
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to deserialize attributesJson", ex);
        }
    }

    protected String writeAttributes(Map<String, Object> attributes) {
        try {
            return objectMapper.writeValueAsString(attributes != null ? attributes : Map.of());
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize attributes", ex);
        }
    }
}