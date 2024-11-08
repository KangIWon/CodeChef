package com.sparta.codechef.domain.chat.v3_redis.entity;

import com.sparta.codechef.domain.chat.v3_redis.dto.ChatUserDto;
import com.sparta.codechef.domain.chat.v3_redis.enums.ChatUserRole;
import com.sparta.codechef.security.AuthUser;
import lombok.Getter;

@Getter
public class ChatUser {

    private final Long id;
    private final String email;
    private final ChatUserRole role;
    private final Long roomId;

    private ChatUser(Long id, String email, ChatUserRole role, Long roomId) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.roomId = roomId;
    }

    public static ChatUser of(ChatUserDto chatUser) {
        return new ChatUser(
                chatUser.getId(),
                chatUser.getEmail(),
                ChatUserRole.of(chatUser.getRole()),
                chatUser.getRoomId() == null ? null : chatUser.getRoomId()
        );
    }

    public ChatUser updateChatRoom(Long roomId) {
        return new ChatUser(this.id, this.email, this.role, roomId);
    }

    public static ChatUser fromAuthUser(AuthUser authUser) {
        return new ChatUser(authUser.getUserId(), authUser.getEmail(), ChatUserRole.of(authUser.getUserRole()), null);
    }

    public ChatUser updateRoleAsHOST() {
        return new ChatUser(this.id, this.email, ChatUserRole.ROLE_HOST, this.roomId);
    }

    public ChatUser updateRoleAsUSER() {
        return new ChatUser(this.id, this.email, ChatUserRole.ROLE_USER, this.roomId);
    }

    public ChatUser createChatRoom(Long roomId) {
        return new ChatUser(this.id, this.email, ChatUserRole.ROLE_HOST, roomId);
    }
}
