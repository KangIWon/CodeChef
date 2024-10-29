package com.sparta.codechef.domain.chat.v2.repository;

import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.exception.ApiException;
import com.sparta.codechef.domain.chat.v1.dto.response.ChatRoomGetResponse;
import com.sparta.codechef.domain.chat.v2.entity.WSChatUser;
import com.sparta.codechef.domain.chat.v2.entity.WSChatRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class WSChatRepositoryImpl implements WSChatRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CHAT_ROOM_ZSET_KEY = "chatRoomZSet";
    private static final String CHAT_ROOM_USER_LIST_KEY = "chatRoomUsers:";
    private static final String CONNECTED_CHAT_USER_LIST_KEY = "connectedChatUsers:";

    @Override
    public Long generateId(String key) {
        return redisTemplate.opsForValue().increment("id:" + key);
    }

    // 채팅방 생성
    @Override
    public void saveChatRoom(WSChatRoom chatRoom) {
        this.redisTemplate.opsForZSet().add(CHAT_ROOM_ZSET_KEY, chatRoom.getId(), chatRoom.getId());
        this.subscribeChatRoom(chatRoom.getId(), chatRoom.getHostId());
    }

    // 채팅방 삭제
    @Override
    public void deleteFromChatRoomList(Long chatRoomId) {
        this.redisTemplate.opsForZSet().remove(CHAT_ROOM_ZSET_KEY, chatRoomId);
        this.redisTemplate.delete(CHAT_ROOM_USER_LIST_KEY + chatRoomId);
    }

    // 채팅방 입장(구독)
    @Override
    public void subscribeChatRoom(Long chatRoomId, Long userId) {
        this.redisTemplate.opsForList().rightPush(CHAT_ROOM_USER_LIST_KEY + chatRoomId, userId);
    }

    // 채팅방 퇴장(구독 해제)
    public void unsubscribeChatRoom(Long chatRoomId, Long userId) {
        this.redisTemplate.opsForList().remove(CHAT_ROOM_USER_LIST_KEY + chatRoomId, 1, userId);
    }

    // 채팅방 현재 접속 유저 count
    @Override
    public Long countJoinedUser(Long chatRoomId) {
        return this.redisTemplate.opsForList().size(CHAT_ROOM_USER_LIST_KEY + chatRoomId);
    }

    // 승계할 다음 방장 조회
    @Override
    public Optional<WSChatUser> findNextHost(Long chatRoomId, Long userId) {
        WSChatUser WSChatUser = (WSChatUser) this.redisTemplate.opsForList().range(CHAT_ROOM_USER_LIST_KEY + chatRoomId, 0, 1);

        if (WSChatUser == null) {
            return Optional.empty();
        }

        return Optional.of(WSChatUser);
    }

    // 방장 여부 조회
    @Override
    public boolean existsByIdAndUserId(Long chatRoomId, Long userId) {
        WSChatRoom chatRoom = (WSChatRoom) this.redisTemplate.opsForValue().get(chatRoomId);

        if (chatRoom == null) {
            throw new ApiException(ErrorStatus.NOT_FOUND_CHATROOM);
        }

        return chatRoom.getHostId().equals(userId);
    }

    // 유저 session 저장
    @Override
    public void saveConnectedChatUser(WebSocketSession session, Long userId) {
        this.redisTemplate.opsForHash().put(CONNECTED_CHAT_USER_LIST_KEY, userId, session);
    }

    // 유저 session 삭제
    @Override
    public void deleteDisconnectedUser(WebSocketSession session, Long userId) {
        this.redisTemplate.opsForHash().delete(CONNECTED_CHAT_USER_LIST_KEY, userId);
    }

    // 전체 채팅방 조회
    @Override
    public Page<ChatRoomGetResponse> getAllChatRooms(int page, int size) {
        int start = (page - 1) * size;
        int end = start + size - 1;

        Long totalChatRoom = this.redisTemplate.opsForZSet().size(CHAT_ROOM_ZSET_KEY);
        if (totalChatRoom == null) {
            totalChatRoom = 0L;
        }

        Set<Object> chatRoomSet = this.redisTemplate.opsForZSet().range(CHAT_ROOM_ZSET_KEY, start, end);

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id"));

        if (chatRoomSet != null) {
            List<ChatRoomGetResponse> chatRoomList = chatRoomSet.stream()
                    .map(object -> (WSChatRoom) object)
                    .map(chatRoom -> new ChatRoomGetResponse(
                                    chatRoom.getId(),
                                    chatRoom.getTitle(),
                                    chatRoom.getPassword() != null,
                                    this.countJoinedUser(chatRoom.getId()),
                                    chatRoom.getMaxParticipants()
                            )
                    ).toList();

            return new PageImpl<>(chatRoomList, pageable, totalChatRoom);
        }

        return new PageImpl<>(List.of(), pageable, 0);
    }
}
