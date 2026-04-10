package com.axioma.aion.identity.adapter.in.web;

import com.axioma.aion.identity.adapter.in.web.dto.AuthenticateRequestDto;
import com.axioma.aion.identity.adapter.in.web.dto.AuthenticateResponseDto;
import com.axioma.aion.identity.adapter.in.web.mapper.IdentityWebMapper;
import com.axioma.aion.identity.domain.model.AuthenticationResult;
import com.axioma.aion.identity.domain.model.SecurityRequest;
import com.axioma.aion.identity.domain.port.in.AuthenticateRequestUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/identity")
@RequiredArgsConstructor
public class IdentityController {

    private final AuthenticateRequestUseCase authenticateRequestUseCase;
    private final IdentityWebMapper identityWebMapper;

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticateResponseDto> authenticate(
            @Valid @RequestBody AuthenticateRequestDto requestDto,
            @RequestHeader(value = "X-Forwarded-For", required = false) String forwardedFor,
            @RequestHeader(value = "User-Agent", required = false) String userAgent
    ) {
        SecurityRequest securityRequest = identityWebMapper.toDomain(
                requestDto,
                forwardedFor,
                userAgent
        );

        AuthenticationResult result = authenticateRequestUseCase.authenticate(securityRequest);

        if (!result.success()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(identityWebMapper.toErrorResponse(result));
        }

        return ResponseEntity.ok(identityWebMapper.toSuccessResponse(result));
    }
}