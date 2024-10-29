package com.sparta.codechef.domain.chat.v2.repository;

import com.sparta.codechef.domain.chat.v1.dto.response.ChatRoomGetResponse;
import com.sparta.codechef.domain.chat.v2.entity.WSChatUser;
import com.sparta.codechef.domain.chat.v2.entity.WSChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.web.socket.WebSocketSession;

import java.util.Optional;

public interface WSChatRepository {
    Long generateId(String key);

    void saveChatRoom(WSChatRoom chatRoom);

    Page<ChatRoomGetResponse> getAllChatRooms(int page, int size);

    void subscribeChatRoom(Long chatRoomId, Long userId);

    Long countJoinedUser(Long chatRoomId);

    Optional<WSChatUser> findNextHost(Long chatRoomId, Long userId);

    void deleteFromChatRoomList(Long chatRoomId);

    boolean existsByIdAndUserId(Long chatRoomId, Long userId);

    void saveConnectedChatUser(WebSocketSession session, Long userId);

    void deleteDisconnectedUser(WebSocketSession session, Long userId);
}
