package com.axioma.aion.identitygateway.adapter.out.security;

import com.axioma.aion.identitygateway.domain.port.out.CredentialHashVerifierPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordEncoderCredentialHashVerifierAdapter implements CredentialHashVerifierPort {

    private final PasswordEncoder passwordEncoder;

    public PasswordEncoderCredentialHashVerifierAdapter(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean matches(String rawValue, String storedHash) {
        if (rawValue == null || storedHash == null || rawValue.isBlank() || storedHash.isBlank()) {
            return false;
        }
        return passwordEncoder.matches(rawValue, storedHash);
    }
}