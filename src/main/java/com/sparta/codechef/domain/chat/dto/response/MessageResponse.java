package com.sparta.codechef.domain.chat.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class MessageResponse {
    private final Long id;
    private final String message;
    private final LocalDateTime createdAt;
}
