package com.sparta.codechef.domain.chat.dto.response;

import com.sparta.codechef.domain.user.dto.response.UserResponse;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MessageResponse {
    private final Long id;
    private final String message;
    private final UserResponse user;
    private final LocalDateTime createdAt;

    public MessageResponse(Long id, String message, UserResponse user, LocalDateTime createdAt) {
        this.id = id;
        this.message = message;
        this.user = user;
        this.createdAt = createdAt;
    }

    public MessageResponse(Long id, String message, Long userId, String email, LocalDateTime createdAt) {
        this.id = id;
        this.message = message;
        this.user = new UserResponse(userId, email);
        this.createdAt = createdAt;
    }


}
