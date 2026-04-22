package com.axioma.aion.identitygateway.adapter.out.persistence.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("identity_credential")
public class IdentityCredentialEntity {

    @Id
    private UUID id;

    private UUID identityContextId;
    private String credentialType;
    private String credentialKey;
    private String secretHash;
    private String status;
    private Boolean enabled;
}