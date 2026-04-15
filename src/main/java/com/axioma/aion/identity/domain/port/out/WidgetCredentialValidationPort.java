package com.axioma.aion.identity.domain.port.out;

import com.axioma.aion.identity.domain.model.WidgetValidationResult;

public interface WidgetCredentialValidationPort {
    WidgetValidationResult validate(String widgetKey, String origin, String channel);
}