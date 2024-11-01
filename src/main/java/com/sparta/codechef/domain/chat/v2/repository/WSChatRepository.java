package com.sparta.codechef.domain.chat.v2.repository;

import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.exception.ApiException;
import com.sparta.codechef.domain.chat.v1.dto.response.ChatRoomGetResponse;
import com.sparta.codechef.domain.chat.v2.entity.WSChatRoom;
import com.sparta.codechef.domain.chat.v2.entity.WSChatUser;
import com.sparta.codechef.domain.chat.v2.entity.WSChatUserRole;
import com.sparta.codechef.domain.chat.v2.entity.WSMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Repository
@RequiredArgsConstructor
public class WSChatRepository {



    private final RedisTemplate<String, String> stringRedisTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final WSChatRoomRepository chatRoomRepository;
    private final WSChatUserRepository chatUserRepository;
    private final WSMessageRepository messageRepository;

    private static final String CHAT_ROOM_MESSAGE_KEY = "chatRoomMessages";
    private static final String SUBSCRIBE_CHAT_ROOM_KEY = "subChatRoom";
    private static final String CHAT_ROOM_KEY = "chatRoom:";
    private static final String CHAT_USER_KEY = "chatUser:";



    // 현재 등록된 마지막 ID
    private Long getMaxId(String key) {
        String maxId = (String) stringRedisTemplate.opsForValue().get("id:" + key);
        if (maxId == null) {
            throw new ApiException(ErrorStatus.NO_ID_OF_KEY);
        }
        return Long.parseLong(maxId);
    }

    // ID 생성 메서드
    public Long generateId(String key) {
        return this.redisTemplate.opsForValue().increment("id:" + key);
    }

    /**
     * 채팅방 입장(구독)
     *   - 유저 구독자 ZSet에 추가
     *   - 채팅방 구독자수 +1
     *   - 유저 구독 채팅방 ID 업데이트
     * @param roomId : 채팅방 id
     * @param userId : 채팅 유저 id
     */
    public void subscribeChatRoom(Long roomId, Long userId) {
        this.redisTemplate.opsForZSet().add(SUBSCRIBE_CHAT_ROOM_KEY,  userId, roomId);
        this.redisTemplate.opsForHash().put(CHAT_USER_KEY + userId, "roomId", roomId);
        this.redisTemplate.opsForHash().increment(CHAT_ROOM_KEY + roomId, "curParticipants", 1);
    }


    /**
     * 채팅방 퇴장(구독 해제)
     *   - 유저 구독자 ZSet에서 제거
     *   - 채팅방 현재 구독자 수 -1
     *   - 유저의 구독 채팅방 id 삭제
     *   - 구독자가 없으면 채팅방 삭제
     *   - 퇴장한 유저가 방장일 때, 승계할 유저 ID 반환
     *
     * @param roomId : 채팅방 ID
     * @param userId : 유저 ID
     */
    public void unsubscribeChatRoom(Long roomId, Long userId) {
        this.redisTemplate.opsForZSet().remove(SUBSCRIBE_CHAT_ROOM_KEY, userId);
        this.redisTemplate.opsForHash().delete(CHAT_USER_KEY + userId, "roomId");
        Long total = this.redisTemplate.opsForHash().increment(CHAT_ROOM_KEY + roomId, "curParticipants",-1);

        if (total == 0) {
            this.redisTemplate.delete(CHAT_ROOM_KEY + roomId);
        }

        if (this.isHost(userId)) {
            this.successChatRoomHost(roomId, userId);
        }
    }

    /**
     * 채팅방 방장 승계할 유저 ID getter
     * @param roomId : 채팅방 ID
     */
    public void successChatRoomHost(Long roomId, Long userId) {
        Set<String> userIdSet = this.stringRedisTemplate.opsForZSet().range(SUBSCRIBE_CHAT_ROOM_KEY, roomId, roomId);

        if (userIdSet == null || userIdSet.isEmpty()) {
            return;
        }

        Long nextHostId = Long.parseLong(userIdSet.stream().toList().get(0));

        WSChatUser chatUser = this.chatUserRepository.findById(userId).orElseThrow(
                () -> new ApiException(ErrorStatus.NOT_FOUND_CHAT_USER)
        );

        chatUser.updateRole(WSChatUserRole.ROLE_USER);
        this.chatUserRepository.save(chatUser);

        WSChatUser nextHost = this.chatUserRepository.findById(nextHostId).orElseThrow(
                () -> new ApiException(ErrorStatus.NOT_FOUND_CHAT_USER)
        );

        nextHost.updateRole(WSChatUserRole.ROLE_HOST);
        this.chatUserRepository.save(nextHost);

        this.redisTemplate.opsForHash().put(CHAT_ROOM_KEY + roomId, "hostId", nextHostId);
    }


    // 현재 채팅 접속 중인 유저인지 확인
    public boolean isConnected(Long userId) {
        return this.redisTemplate.opsForHash().get(CHAT_USER_KEY + userId, "id") != null;
    }

    // 현재 채팅방에 접속해 있는 유저인지 확인
    public boolean isInChatRoom(Long userId) {
        Long roomId = (Long) this.redisTemplate.opsForHash().get(CHAT_USER_KEY + userId, "chatRoomId");
        return roomId != null;
    }


    // 채팅방 방장 여부 조회
    public boolean isHost(Long userId) {
        String role = (String) this.stringRedisTemplate.opsForHash().get(CHAT_USER_KEY + userId, "role");

        return Objects.equals(role, "ROLE_HOST");
    }


    // 전체 채팅방 조회
    public Page<ChatRoomGetResponse> findAllChatRooms(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id"));

        int start = (page - 1) * size;
        int end = start + size;


        List<WSChatRoom> chatRoomList = new ArrayList<>();
        this.chatRoomRepository.findAll().forEach(chatRoomList::add);

        long totalChatRoom = chatRoomList.size();

        if (totalChatRoom < start || totalChatRoom == 0) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        chatRoomList = chatRoomList.stream().filter(Objects::nonNull).toList();
        List<ChatRoomGetResponse> responseList = IntStream.range(start, (int) Math.min(end, totalChatRoom))
                .mapToObj(chatRoomList::get)
                .map(chatRoom -> new ChatRoomGetResponse(
                        chatRoom.getId(),
                        chatRoom.getTitle(),
                        chatRoom.getPassword(),
                        chatRoom.getCurParticipants(),
                        chatRoom.getMaxParticipants()
                ))
                .sorted((o1, o2) -> (int) (o1.getId() - o2.getId()))
                .toList();

        return new PageImpl<>(responseList, pageable, totalChatRoom);
    }


    /**
     * 메세지 리스트 전체 조회
     * @param roomId : 채팅방 ID
     * @return
     */
    public List<WSMessage> getMessagesByRoomId(Long roomId) {
        List<WSMessage> messageList = new ArrayList<>();

        try {
            List<Long> messageIdList = Objects.requireNonNull(this.redisTemplate.opsForZSet().range(CHAT_ROOM_MESSAGE_KEY, roomId, roomId))
                    .stream()
                    .map(o -> Long.parseLong(String.valueOf(o)))
                    .toList();
            this.messageRepository.findAllById(messageIdList).forEach(messageList::add);

            return messageList.stream().filter(Objects::nonNull)
                    .sorted((m1, m2) -> (int)(m1.getId() - m2.getId()))
                    .toList();

        } catch (NullPointerException e) {
            return messageList;
        }
    }


    /**
     * 메세지 전체 조회 with 페이징
     * @param page : 페이지 번호
     * @param size : 페이지 크기
     * @param roomId : 채팅방 Id
     * @return
     */
    public Page<WSMessage> getMessagesByRoomId(int page, int size, Long roomId) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id"));

        int start = (page - 1) * size;
        int end = start + size;


        List<WSMessage> messageList = new ArrayList<>();
        try {
            List<Long> messageIdList = Objects.requireNonNull(this.redisTemplate.opsForZSet().range(CHAT_ROOM_MESSAGE_KEY, roomId, roomId))
                    .stream()
                    .map(o -> Long.parseLong(String.valueOf(o)))
                    .toList();
            this.messageRepository.findAllById(messageIdList).forEach(messageList::add);
            long totalMessage = messageList.size();

            if (totalMessage < start || totalMessage == 0) {
                return new PageImpl<>(List.of(), pageable, 0);
            }

            messageList = messageList.stream().filter(Objects::nonNull)
                    .sorted((m1, m2) -> (int)(m1.getId() - m2.getId()))
                    .toList();

            messageList = IntStream.range(start, (int) Math.min(end, totalMessage))
                    .mapToObj(messageList::get)
                    .toList();

            return new PageImpl<>(messageList, pageable, totalMessage);

        } catch (NullPointerException e) {
            return null;
        }
    }

    public void saveMessage(WSMessage wsMessage) {
        this.redisTemplate.opsForZSet().add(CHAT_ROOM_MESSAGE_KEY, wsMessage.getId(), wsMessage.getRoomId());
        this.messageRepository.save(wsMessage);
    }

}

