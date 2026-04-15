package com.axioma.aion.identity.adapter.in.web;

import com.axioma.aion.identity.adapter.in.web.dto.CreateChannelSessionRequestDto;
import com.axioma.aion.identity.adapter.in.web.dto.CreateChannelSessionResponseDto;
import com.axioma.aion.identity.adapter.in.web.mapper.ChannelSessionWebMapper;
import com.axioma.aion.identity.domain.model.ChannelSessionResult;
import com.axioma.aion.identity.domain.model.CreateChannelSessionRequest;
import com.axioma.aion.identity.domain.port.in.CreateChannelSessionUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/identity/channel")
@RequiredArgsConstructor
public class ChannelSessionController {

    private final CreateChannelSessionUseCase createChannelSessionUseCase;
    private final ChannelSessionWebMapper channelSessionWebMapper;

    @PostMapping("/session")
    public ResponseEntity<CreateChannelSessionResponseDto> createSession(
            @Valid @RequestBody CreateChannelSessionRequestDto requestDto,
            @RequestHeader(value = "X-Forwarded-For", required = false) String forwardedFor,
            @RequestHeader(value = "User-Agent", required = false) String userAgent
    ) {
        CreateChannelSessionRequest request = channelSessionWebMapper.toDomain(
                requestDto,
                forwardedFor,
                userAgent
        );

        ChannelSessionResult result = createChannelSessionUseCase.create(request);

        return ResponseEntity.ok(channelSessionWebMapper.toResponse(result));
    }
}