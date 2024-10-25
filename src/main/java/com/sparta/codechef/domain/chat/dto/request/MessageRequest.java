package com.sparta.codechef.domain.chat.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class MessageRequest {
    private final String message;

    @JsonCreator
    public MessageRequest(@JsonProperty("message") String message) {
        this.message = message;
    }
}
