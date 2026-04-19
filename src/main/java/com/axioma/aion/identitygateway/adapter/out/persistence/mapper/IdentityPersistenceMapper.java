package com.axioma.aion.identitygateway.adapter.out.persistence.mapper;

import com.axioma.aion.identitygateway.adapter.out.persistence.entity.IdentitySessionEntity;
import com.axioma.aion.identitygateway.domain.model.IdentitySession;
import com.axioma.aion.identitygateway.domain.model.valueobject.TokenId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IdentityPersistenceMapper {

    @Mapping(target = "tokenId", source = "sessionTokenId")
    IdentitySession toDomain(IdentitySessionEntity entity);

    @Mapping(target = "sessionTokenId", source = "tokenId")
    IdentitySessionEntity toEntity(IdentitySession identitySession);

    default TokenId map(String value) {
        return value == null ? null : new TokenId(value);
    }

    default String map(TokenId tokenId) {
        return tokenId == null ? null : tokenId.value();
    }
}