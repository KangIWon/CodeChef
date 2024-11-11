package com.sparta.codechef.domain.chat.v1.dto.request;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MessageRequest {
    private final String content;
}
