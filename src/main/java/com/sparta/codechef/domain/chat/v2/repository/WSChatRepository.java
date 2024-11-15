package com.sparta.codechef.domain.chat.v2.repository;

import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.exception.ApiException;
import com.sparta.codechef.domain.chat.v1.dto.response.ChatRoomGetResponse;
import com.sparta.codechef.domain.chat.v2.dto.UnsubscribeDTO;
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
    private static final String CHAT_USER_LIST_KEY = "chatUserList:";
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
     *   - 유저 구독자 리스트에 추가
     *   - 채팅방 구독자수 +1
     *   - 유저 구독 채팅방 ID 업데이트
     * @param roomId : 채팅방 id
     * @param userId : 채팅 유저 id
     */
    public void subscribeChatRoom(Long roomId, Long userId) {
        this.redisTemplate.opsForList().rightPush(CHAT_USER_LIST_KEY + roomId,  userId);
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
    public UnsubscribeDTO unsubscribeChatRoom(Long roomId, Long userId) {
        this.redisTemplate.opsForZSet().remove(SUBSCRIBE_CHAT_ROOM_KEY, userId);
        this.redisTemplate.opsForHash().delete(CHAT_USER_KEY + userId, "roomId");
        this.redisTemplate.opsForHash().increment(CHAT_ROOM_KEY + roomId, "curParticipants",-1);

        boolean isHost = this.isHost(userId);
        Long hostId = null;
        if (isHost) {
            hostId = this.successChatRoomHost(roomId, userId);
        }

        return new UnsubscribeDTO(isHost, hostId);
    }

    /**
     * 채팅방 방장 승계할 유저 ID getter
     * @param roomId : 채팅방 ID
     * @param userId : 퇴장하는 유저 ID
     */
    public Long successChatRoomHost(Long roomId, Long userId) {
        WSChatUser chatUser = this.chatUserRepository.findById(userId).orElseThrow(
                () -> new ApiException(ErrorStatus.NOT_FOUND_CHAT_USER)
        );

        chatUser.updateRole(WSChatUserRole.ROLE_USER);
        this.chatUserRepository.save(chatUser);

        this.redisTemplate.opsForList().remove(CHAT_USER_LIST_KEY + roomId, 0, userId);
        List<String> nextHostIdList = this.stringRedisTemplate.opsForList().range(CHAT_USER_LIST_KEY + roomId, 0, 0);

        if (nextHostIdList == null || nextHostIdList.isEmpty()) {
            return null;
        }

        Long nextHostId = Long.parseLong(nextHostIdList.get(0));
        WSChatUser nextHost = this.chatUserRepository.findById(nextHostId).orElseThrow(
                () -> new ApiException(ErrorStatus.NOT_FOUND_CHAT_USER)
        );

        nextHost.updateRole(WSChatUserRole.ROLE_HOST);
        this.chatUserRepository.save(nextHost);

        this.redisTemplate.opsForHash().put(CHAT_ROOM_KEY + roomId, "hostId", nextHostId);

        return nextHostId;
    }


    // 현재 채팅 접속 중인 유저인지 확인
    public boolean isConnected(Long userId) {
        return this.redisTemplate.opsForHash().get(CHAT_USER_KEY + userId, "id") != null;
    }

    // 현재 채팅방에 접속해 있는 유저인지 확인
    public boolean isInChatRoom(Long userId) {
        return this.stringRedisTemplate.opsForHash().get(CHAT_USER_KEY + userId, "chatRoomId") != null;
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

        if (totalChatRoom <= start || totalChatRoom == 0) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
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
                .sorted((o1, o2) -> (int) (o1.getId() - o2.getId()))  // 채팅방 ID로 오름차순 정렬
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
            List<Long> messageIdList = Objects.requireNonNull(
                        this.stringRedisTemplate.opsForZSet().rangeByScore(CHAT_ROOM_MESSAGE_KEY, roomId, roomId)
                    )
                    .stream()
                    .map(Long::parseLong)
                    .toList();

            return messageIdList.stream()
                    .map(this.messageRepository::findById)
                    .map(WSMessage.class::cast)
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

        try {
            List<Long> messageIdList = Objects.requireNonNull(
                        this.stringRedisTemplate.opsForZSet().rangeByScore(CHAT_ROOM_MESSAGE_KEY, roomId, roomId)
                    )
                    .stream()
                    .map(Long::parseLong)
                    .toList();
            long totalMessage = messageIdList.size();

            if (totalMessage < start || totalMessage == 0) {
                return new PageImpl<>(new ArrayList<>(), pageable, 0);
            }

            List<WSMessage> messageList = IntStream.range(start, (int) Math.min(end, totalMessage))
                    .mapToObj(messageIdList::get)
                    .map(this.messageRepository::findById)
                    .map(WSMessage.class::cast)
                    .toList();

            return new PageImpl<>(messageList, pageable, totalMessage);

        } catch (NullPointerException e) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    public WSMessage saveMessage(WSMessage wsMessage) {
        this.redisTemplate.opsForZSet().add(CHAT_ROOM_MESSAGE_KEY, wsMessage.getId(), wsMessage.getRoomId());
        return this.messageRepository.save(wsMessage);
    }


    /**
     * 채팅방 개설 유저인지 확인 메서드
     * @param roomId : 채팅방 ID
     * @param userId : 방장인지 확인할 유저 ID
     * @return
     */
    public boolean existChatRoomByIdAndHostId(Long roomId, Long userId) {
        String hostId = (String) this.stringRedisTemplate.opsForHash().get(CHAT_ROOM_KEY + roomId, "hostId");
        boolean existChatRoom =  hostId != null && hostId.equals(String.valueOf(userId));
        return existChatRoom;
    }

    /**
     * 채팅 유저 id로 email getter
     * @param userId : 채팅 유저 ID
     * @return 채팅 유저 Email
     */
    public String findEmailById(Long userId) {
        return (String) this.stringRedisTemplate.opsForHash().get(CHAT_USER_KEY + userId, "email");
    }
}

