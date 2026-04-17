package com.axioma.aion.identitygateway.domain.port.out;

public interface CredentialHashVerifierPort {

    boolean matches(String rawValue, String storedHash);
}