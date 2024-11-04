package com.sparta.codechef.domain.chat.v1.dto.response;

import com.sparta.codechef.domain.user.dto.response.UserResponse;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MessageResponse {
    private final Long id;
    private final String content;
    private final UserResponse user;
    private final LocalDateTime createdAt;

    public MessageResponse(Long id, String content, Long userId, String email, LocalDateTime createdAt) {
        this.id = id;
        this.content = content;
        this.user = new UserResponse(userId, email);
        this.createdAt = createdAt;
    }


}
