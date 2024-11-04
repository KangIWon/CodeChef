package com.sparta.codechef.domain.chat.v2.entity;

import com.sparta.codechef.security.AuthUser;
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
public class WSChatUser implements Serializable {

    @Id
    private Long id;
    @NotNull
    private String email;
    private Long roomId;
    @NotNull
    private String role;

    public void updateChatRoom(Long roomId) {
        this.roomId = roomId;
    }

    public void updateRole(WSChatUserRole role) { this.role = role.name(); }

    public static WSChatUser fromAuthUser(AuthUser authUser) {
        return new WSChatUser(authUser.getUserId(), authUser.getEmail(), null, WSChatUserRole.of(authUser.getUserRole()).name());
    }
}
