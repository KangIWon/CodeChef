package com.sparta.codechef.domain.chat.v3_redisPubSub.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UnsubscribeDto {
    private final boolean success;
    private final Long nextHostId;
}