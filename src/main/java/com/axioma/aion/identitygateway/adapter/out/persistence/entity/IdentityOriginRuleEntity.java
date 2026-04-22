package com.axioma.aion.identitygateway.adapter.out.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("identity_origin_rule")
public class IdentityOriginRuleEntity {

    @Id
    private UUID id;

    private UUID identityCredentialId;
    private String allowedOrigin;
    private String status;
    private OffsetDateTime createDate;
    private OffsetDateTime updateDate;
    private Boolean activeRegInd;
}
