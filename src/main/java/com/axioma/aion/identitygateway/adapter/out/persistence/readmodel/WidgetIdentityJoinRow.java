package com.axioma.aion.identitygateway.adapter.out.persistence.readmodel;

import lombok.Builder;

@Builder
public record WidgetIdentityJoinRow(
        String identityContextId,
        String tenantId,
        String channel,
        String subjectType,
        String subjectValue,
        String credentialValueHash,
        String originType,
        String originValue,
        Integer originPriority
) {
}