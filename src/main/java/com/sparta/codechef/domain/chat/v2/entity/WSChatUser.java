package com.sparta.codechef.domain.chat.v2.entity;

import com.sparta.codechef.common.enums.UserRole;
import com.sparta.codechef.security.AuthUser;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@Getter
@Builder
@RedisHash("ChatUser")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WSChatUser implements Serializable {

    @Id
    private Long id;
    @NotNull
    private String email;
    private Long chatRoomId;
    @Builder.Default
    private WSChatUserRole role = WSChatUserRole.ROLE_USER;

    public void updateChatRoom(Long chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public void updateRole(WSChatUserRole role) { this.role = role; }

    public static WSChatUser fromAuthUser(AuthUser authUser) {
        return WSChatUser.builder()
                .id(authUser.getUserId())
                .email(authUser.getEmail())
                .role(WSChatUserRole.of((UserRole) authUser.getAuthorities().toArray()[0]))
                .build();
    }
}
