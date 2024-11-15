package com.sparta.codechef.domain.chat.v3_redisPubSub.entity;

import com.sparta.codechef.domain.chat.v3_redisPubSub.dto.ChatUserDto;
import com.sparta.codechef.domain.chat.v3_redisPubSub.enums.ChatUserRole;
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

    public static ChatUser fromAuthUser(AuthUser authUser) {
        return new ChatUser(authUser.getUserId(), authUser.getEmail(), ChatUserRole.of(authUser.getUserRole()), null);
    }

    /**
     * 방장으로 권한 변경
     * @return
     */
    public ChatUser updateRoleAsHOST() {
        return new ChatUser(this.id, this.email, ChatUserRole.ROLE_HOST, this.roomId);
    }

    /**
     * 채팅방 구독
     * @param roomId : 채팅방 ID
     * @return
     */
    public ChatUser subscribeChatRoom(Long roomId) {
        return new ChatUser(this.id, this.email, ChatUserRole.ROLE_USER, roomId);
    }

    /**
     * 채팅방 구독 & 방장 권한
     * @param roomId : 채팅방 ID
     * @return
     */
    public ChatUser subscribeChatRoomAsHost(Long roomId) {
        return new ChatUser(this.id, this.email, ChatUserRole.ROLE_HOST, roomId);
    }

    /**
     * 채팅방 구독 취소
     *   - 유저 권한 : ROLE_USER
     *   - 구독 채팅방 ID : null
     * @return ChatUser 객체
     */
    public ChatUser unsubscribeChatRoom() {

        return new ChatUser(this.id, this.email, ChatUserRole.ROLE_USER, null);
    }
}
