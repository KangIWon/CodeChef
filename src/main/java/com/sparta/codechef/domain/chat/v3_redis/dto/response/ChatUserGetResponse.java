package com.sparta.codechef.domain.chat.v3_redis.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ChatUserGetResponse {
    private final String id;
    private final String email;
}
