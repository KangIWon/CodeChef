package com.sparta.codechef.domain.chat.v3_redisPubSub.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SenderDto {
    private final Long id;
    private final String email;
}
