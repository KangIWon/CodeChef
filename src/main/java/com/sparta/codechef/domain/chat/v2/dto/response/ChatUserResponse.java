package com.sparta.codechef.domain.chat.v2.dto.response;

import com.sparta.codechef.domain.chat.v2.entity.WSChatUser;
import com.sparta.codechef.security.AuthUser;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatUserResponse {
    private final Long id;
    private final String email;

    public ChatUserResponse(WSChatUser chatUser) {
        this.id = chatUser.getId();
        this.email = chatUser.getEmail();
    }
}
