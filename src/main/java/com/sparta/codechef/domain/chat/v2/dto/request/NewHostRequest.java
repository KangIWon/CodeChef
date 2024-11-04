package com.sparta.codechef.domain.chat.v2.dto.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class NewHostRequest {
    private final Long userId;
    private final String hostEmail;
}
