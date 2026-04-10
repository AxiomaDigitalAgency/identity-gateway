package com.axioma.aion.identity.config;

import com.axioma.aion.identity.domain.model.SecurityMode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "aion.identity")
public class IdentityProperties {

    private boolean enabled = true;
    private SecurityMode mode = SecurityMode.CHANNEL;
}