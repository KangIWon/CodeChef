package com.sparta.codechef.domain.chat.v2.dto;

import com.sparta.codechef.domain.chat.v2.entity.WSChatUser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class NewHostRequest {
    private final String hostEmail;
}
