package com.sparta.codechef.domain.chat.v3_redis.dto;

import com.sparta.codechef.domain.chat.v3_redis.entity.ChatUser;
import com.sparta.codechef.domain.chat.v3_redis.enums.ChatUserRole;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("chatUser")
public class ChatUserDto implements Serializable {
    @Id
    private Long id;
    @NotNull
    private String email;
    @NotNull
    private ChatUserRole role;
    private Long roomId;

    public static ChatUserDto fromChatUser(ChatUser chatUser) {
        return new ChatUserDto(
                chatUser.getId(),
                chatUser.getEmail(),
                chatUser.getRole(),
                chatUser.getRoomId() == null ? null : chatUser.getRoomId()
        );
    }

    public String getRole() {
        return this.role.name();
    }

    public void setRole(String role) {
        this.role = ChatUserRole.of(role);
    }
}
