package com.axioma.aion.identitygateway.adapter.in.web;

import com.axioma.aion.identitygateway.adapter.in.web.dto.RevokeSessionRequest;
import com.axioma.aion.identitygateway.adapter.in.web.dto.RevokeSessionResponse;
import com.axioma.aion.identitygateway.adapter.in.web.dto.ValidateSessionRequest;
import com.axioma.aion.identitygateway.adapter.in.web.dto.ValidateSessionResponse;
import com.axioma.aion.identitygateway.application.command.RevokeSessionCommand;
import com.axioma.aion.identitygateway.application.command.ValidateSessionCommand;
import com.axioma.aion.identitygateway.application.result.RevokeSessionResult;
import com.axioma.aion.identitygateway.application.result.ValidateSessionResult;
import com.axioma.aion.identitygateway.application.service.RevokeSessionService;
import com.axioma.aion.identitygateway.application.service.ValidateSessionService;
import com.axioma.aion.identitygateway.domain.port.out.JwtSessionProviderPort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/internal/identity/session")
@RequiredArgsConstructor
public class SessionController {

    private final ValidateSessionService validateSessionService;
    private final RevokeSessionService revokeSessionService;
    private final JwtSessionProviderPort jwtSessionProviderPort;

    @PostMapping("/validate")
    public Mono<ValidateSessionResponse> validate(@Valid @RequestBody ValidateSessionRequest request) {
        return validateSessionService.execute(
                        ValidateSessionCommand.builder()
                                .sessionToken(request.sessionToken())
                                .build()
                )
                .map(this::toValidateResponse);
    }

    @PostMapping("/revoke")
    public Mono<RevokeSessionResponse> revoke(@Valid @RequestBody RevokeSessionRequest request) {
        return jwtSessionProviderPort.parse(request.sessionToken())
                .flatMap(claims -> revokeSessionService.execute(
                        RevokeSessionCommand.builder()
                                .tokenId(claims.tokenId())
                                .reason(request.reason())
                                .requestedBy(request.requestedBy())
                                .build()
                ))
                .map(this::toRevokeResponse);
    }

    private ValidateSessionResponse toValidateResponse(ValidateSessionResult result) {
        return ValidateSessionResponse.builder()
                .valid(true)
                .authContext(result.authContext())
                .build();
    }

    private RevokeSessionResponse toRevokeResponse(RevokeSessionResult result) {
        return RevokeSessionResponse.builder()
                .sessionId(result.sessionId())
                .revoked(result.revoked())
                .build();
    }
}