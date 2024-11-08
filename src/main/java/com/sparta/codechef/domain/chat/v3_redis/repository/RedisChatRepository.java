package com.sparta.codechef.domain.chat.v3_redis.repository;

import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.exception.ApiException;
import com.sparta.codechef.domain.chat.v3_redis.dto.ChatRoomDto;
import com.sparta.codechef.domain.chat.v3_redis.dto.ChatUserDto;
import com.sparta.codechef.domain.chat.v3_redis.dto.MessageDto;
import com.sparta.codechef.domain.chat.v3_redis.dto.UnsubscribeDto;
import com.sparta.codechef.domain.chat.v3_redis.dto.response.ChatRoomGetResponse;
import com.sparta.codechef.domain.chat.v3_redis.dto.response.MessageGetResponse;
import com.sparta.codechef.domain.chat.v3_redis.entity.ChatRoom;
import com.sparta.codechef.domain.chat.v3_redis.entity.ChatUser;
import com.sparta.codechef.domain.chat.v3_redis.entity.Message;
import com.sparta.codechef.domain.chat.v3_redis.enums.ChatUserRole;
import com.sparta.codechef.domain.chat.v3_redis.enums.RedisKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.IntStream;

import static com.sparta.codechef.domain.chat.v3_redis.enums.ChatUserRole.*;
import static com.sparta.codechef.domain.chat.v3_redis.enums.RedisHashKey.*;
import static com.sparta.codechef.domain.chat.v3_redis.enums.RedisKey.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisChatRepository {



    private final RedisTemplate<String, String> stringRedisTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisChatRoomRepository chatRoomRepository;
    private final RedisChatUserRepository chatUserRepository;
    private final RedisMessageRepository messageRepository;


    // 현재 등록된 마지막 ID
    private Long getChatRoomMaxId() {
        try {
            String maxId = Objects.requireNonNull(this.stringRedisTemplate.opsForValue().get(ID_CHAT_ROOM.getKey()));
            return Long.parseLong(maxId);
        } catch (NullPointerException e) {
            throw new ApiException(ErrorStatus.NO_ID_OF_KEY);
        }
    }

    private Long getChatUserMaxId() {
        try {
            String maxId = Objects.requireNonNull(this.stringRedisTemplate.opsForValue().get(ID_CHAT_USER.getKey()));
            return Long.parseLong(maxId);
        } catch (NullPointerException e) {
            throw new ApiException(ErrorStatus.NO_ID_OF_KEY);
        }
    }


    // ID 생성 메서드
    public Long generateId(RedisKey key) {
        return this.redisTemplate.opsForValue().increment(key.getKey());
    }

    /**
     * 채팅방 입장(구독)
     *   - 유저 구독자 ZSet에 추가
     *   - 채팅방 구독자수 + 1
     *   - 유저 구독 채팅방 ID 업데이트
     * @param roomId : 채팅방 id
     * @param userId : 채팅 유저 id
     */
    public ChatRoomGetResponse subscribeChatRoom(Long roomId, Long userId) {
        this.redisTemplate.opsForZSet().add(SUBSCRIBE_CHAT_ROOM.getKey(),  userId, Double.valueOf(roomId));
        this.redisTemplate.opsForHash().put(CHAT_USER.getKey(userId), ROOM_ID.getHashKey(), roomId);
        this.redisTemplate.opsForHash().increment(CHAT_ROOM.getKey(roomId), CUR_PARTICIPANTS.getHashKey(), 1);

        ChatRoomDto chatRoomDto = this.chatRoomRepository.findById(roomId).orElseThrow(
                () -> new ApiException(ErrorStatus.FAILED_TO_CREATE_CHAT_ROOM)
        );

        return new ChatRoomGetResponse(ChatRoom.of(chatRoomDto));
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
    public UnsubscribeDto unsubscribeChatRoom(Long roomId, Long userId) {
        this.redisTemplate.opsForZSet().remove(SUBSCRIBE_CHAT_ROOM.getKey(), userId);
        this.redisTemplate.opsForHash().delete(CHAT_USER.getKey(userId), ROOM_ID.getHashKey());
        this.redisTemplate.opsForHash().increment(CHAT_ROOM.getKey(roomId), CUR_PARTICIPANTS.getHashKey(),-1);

        boolean isHost = this.isHost(userId);
        Long hostId = null;

        if (isHost) {
            hostId = this.successChatRoomHost(roomId, userId);
        }

        return new UnsubscribeDto(isHost, hostId);
    }


    // ChatRoom

    /**
     * 채팅방 저장
     * @param chatRoom : 채팅방 객체
     */
    public ChatRoomGetResponse saveChatRoom(ChatRoom chatRoom) {
        this.chatRoomRepository.save(ChatRoomDto.fromChatRoom(chatRoom));
        return new ChatRoomGetResponse(chatRoom);
    }


    /**
     * 채팅방 ID로 채팅방 단건 조회
     * @param roomId : 채팅방 ID
     * @return
     */
    public ChatRoom findChatRoomById(Long roomId) {
        ChatRoomDto chatRoomDto = this.chatRoomRepository.findById(roomId).orElseThrow(
                () -> new ApiException(ErrorStatus.NOT_FOUND_CHATROOM)
        );

        return ChatRoom.of(chatRoomDto);
    }



    /**
     * 채팅방 전체 조회
     * @return
     */
    private List<ChatRoom> findAllChatRoom() {
        List<ChatRoom> chatRoomList = new ArrayList<>();
        this.chatRoomRepository.findAll().forEach(chatRoomDto ->
            chatRoomList.add(ChatRoom.of(chatRoomDto))
        );

        return chatRoomList.stream()
                .filter(Objects::nonNull)
                .sorted((c1, c2) -> (int) (c1.getId() - c2.getId()))
                .toList();
    }

    /**
     * 채팅방 전체 조회 with 페이징
     * @param pageable : 페이징 정보
     * @return
     */
    public Page<ChatRoomGetResponse> findAllChatRooms(Pageable pageable) {
        int start = Integer.parseInt(String.valueOf(pageable.getOffset()));
        int end = start + Integer.parseInt(String.valueOf(pageable.getPageSize()));

        List<ChatRoom> chatRoomList = this.findAllChatRoom();
        long totalChatRoom = chatRoomList.size();

        if (totalChatRoom <= start || totalChatRoom == 0) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }

        chatRoomList = chatRoomList.stream().filter(Objects::nonNull).toList();
        List<ChatRoomGetResponse> responseList = IntStream.range(start, (int) Math.min(end, totalChatRoom))
                .mapToObj(chatRoomList::get)
                .map(ChatRoomGetResponse::new)
                .toList();

        return new PageImpl<>(responseList, pageable, totalChatRoom);
    }



    // Message
    /**
     * 채팅방 ID로 채팅 메세지 ID 전체 조회
     * @param roomId
     * @return
     */
    private List<String> findAllMessageIdByChatRoomId(Long roomId) {
        try {
            Double doubleRoomId = Double.valueOf(roomId);
            return Objects.requireNonNull(this.stringRedisTemplate.opsForZSet().rangeByScore(CHAT_ROOM_MESSAGE.getKey(), doubleRoomId, doubleRoomId))
                    .stream()
                    .filter(Objects::nonNull)
                    .sorted()
                    .toList();
        } catch (NullPointerException e) {
            return new ArrayList<>();
        }
    }

    /**
     * 메세지 리스트 전체 조회
     * @param roomId : 채팅방 ID
     * @return
     */
    public List<Message> findAllMessageByChatRoomId(Long roomId) {
        return this.findAllMessageDtoById(roomId).stream()
                .map(Message::fromMessageDto)
                .toList();
    }

    /**
     * 채팅방 ID로 채팅 메세지 전체 조회
     * @param roomId : 채팅방 ID
     * @return
     */
    private List<MessageDto> findAllMessageDtoById(Long roomId) {
        List<MessageDto> messageDtoList = new ArrayList<>();

        List<Long> messageIdList = this.findAllMessageIdByChatRoomId(roomId).stream()
                .filter(Objects::nonNull)
                .mapToLong(Long::parseLong)
                .boxed()
                .toList();

        if (messageIdList.isEmpty()) {
            return messageDtoList;
        }

        Iterable<MessageDto> messageDtos = this.messageRepository.findAllById(messageIdList);

        for (MessageDto messageDto : messageDtos) {
            messageDtoList.add(messageDto);
        }

        return messageDtoList;
    }

    /**
     * 채팅방 ID로 채팅 메세지 전체 조회
     * @param roomId : 채팅방 ID
     * @return
     */
    public List<MessageGetResponse> findAllMessageGetResponseById(Long roomId){
        return this.findAllMessageDtoById(roomId).stream().map(MessageGetResponse::new).toList();
    }

    /**
     * 메세지 전체 조회 with 페이징
     * @param roomId : 채팅방 Id
     * @param pageable : 페이징 정보
     * @return
     */
    public Page<MessageGetResponse> findAllMessageByChatRoomId(Long roomId, Pageable pageable) {
        int start = Integer.parseInt(String.valueOf(pageable.getOffset()));
        int end = start + Integer.parseInt(String.valueOf(pageable.getPageSize()));
        List<MessageGetResponse> messageList = this.findAllMessageGetResponseById(roomId);
        long totalMessage = messageList.size();
        if (totalMessage < start || totalMessage == 0) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }

        List<MessageGetResponse> messageGetResponseList = IntStream.range(start, (int) Math.min(end, totalMessage))
                .mapToObj(messageList::get)
                .toList();

        return new PageImpl<>(messageGetResponseList, pageable, totalMessage);
    }

    /**
     * 메세지 저장
     * @param message : 메세지
     */
    public MessageDto saveMessage(Message message) {
        this.redisTemplate.opsForZSet().add(CHAT_ROOM_MESSAGE.getKey(), message.getId(), message.getRoomId());
        MessageDto messageDto = new MessageDto(message);
        return this.messageRepository.save(messageDto);
    }


    // ChatUser
    /**
     * 채팅 유저 Getter by ID
     * @param userId : 유저 ID
     * @return
     */
    public ChatUser findChatUserById (Long userId){
        ChatUserDto chatUser = this.chatUserRepository.findById(userId).orElseThrow(
                () -> new ApiException(ErrorStatus.NOT_FOUND_CHAT_USER)
        );

        return ChatUser.of(chatUser);
    }

    /**
     * 채팅 유저 권한 업데이트
     * @param userId : 유저 ID
     * @param role : 권한
     */
    public void updateChatUserRole (Long userId, ChatUserRole role){
        this.stringRedisTemplate.opsForHash().put(CHAT_USER.getKey(userId), ROLE.getHashKey(), role.toValue());
    }

    /**
     * 채팅 유저 저장
     * @param chatUser : 채팅 유저
     */
    public void saveChatUser(ChatUser chatUser) {
        ChatUserDto chatUserDto = ChatUserDto.fromChatUser(chatUser);
        this.chatUserRepository.save(chatUserDto);
    }

    /**
     * 유저 ID로 채팅 유저 삭제
     * @param userId : 채팅 유저 ID
     */
    public void deleteChatUserById(Long userId) {
        this.chatUserRepository.deleteById(userId);
    }

    /**
     * 채팅방 방장 승계할 유저 ID getter
     * @param roomId : 채팅방 ID
     * @param userId : 퇴장하는 유저 ID
     */
    public Long successChatRoomHost(Long roomId, Long userId) {
        Set<String> userIdSet = this.findAllChatUserIdByChatRoomId(roomId);

        this.updateChatUserRole(userId, ROLE_USER);

        if (userIdSet == null || userIdSet.isEmpty()) {
            this.redisTemplate.delete(CHAT_ROOM.getKey(roomId));
            return null;
        }

        String strNextHostId = userIdSet.stream().toList().get(0);
        Long nextHostId = Long.parseLong(strNextHostId);
        this.updateChatUserRole(nextHostId, ROLE_HOST);
        this.updateHost(roomId, nextHostId);

        return nextHostId;
    }

    /**
     * 방장 여부 조회
     * @param userId : 유저 ID
     * @return true : 방장 / false : 방장 아닌 유저
     */
    public boolean isHost(Long userId) {
        String role = (String) this.stringRedisTemplate.opsForHash().get(CHAT_USER.getKey(userId), ROLE.getHashKey());

        return Objects.equals(role, ROLE_HOST.toValue());
    }

    /**
     * 채팅방 ID로 채팅방 안 유저 ID 전체 조회
     * @param roomId : 채팅방 ID
     * @return
     */
    private Set<String> findAllChatUserIdByChatRoomId(Long roomId) {
        Double doubleRoomId = Double.valueOf(roomId);
        return this.stringRedisTemplate.opsForZSet().rangeByScore(SUBSCRIBE_CHAT_ROOM.getKey(), doubleRoomId, doubleRoomId);
    }

    /**
     * 채팅방 방장 업데이트
     * @param roomId : 채팅방 ID
     * @param nextHostId : 새 방장 ID
     */
    private void updateHost(Long roomId, Long nextHostId) {
        this.redisTemplate.opsForHash().put(CHAT_USER.getKey(roomId), HOST_ID.getHashKey(), nextHostId.toString());
    }

    // 현재 채팅 접속 중인 유저인지 확인
    public boolean isConnected(Long userId) {
        return this.redisTemplate.opsForHash().get(CHAT_USER.getKey(userId), ID.getHashKey()) != null;
    }

    // 현재 채팅방에 접속해 있는 유저인지 확인
    public boolean isInChatRoom(Long userId) {
        return this.stringRedisTemplate.opsForHash().get(CHAT_USER.getKey(userId), ROOM_ID.getHashKey()) != null;
    }

    /**
     * 채팅방 방장 ID Getter
     * @param roomId : 채팅방 ID
     * @return
     */
    public Long findChatRoomByIDAndGetHostId(Long roomId) {
        String hostId = (String) this.stringRedisTemplate.opsForHash().get(CHAT_ROOM.getKey(roomId), HOST_ID.getHashKey());
        if (hostId == null) {
            return null;
        }

        return Long.parseLong(hostId);
    }

    /**
     * 채팅 유저 id로 email getter
     * @param userId : 채팅 유저 ID
     * @return 채팅 유저 Email
     */
    public String findEmailById(Long userId) {
        return (String) this.stringRedisTemplate.opsForHash().get(CHAT_USER.getKey(userId), EMAIL.getHashKey());
    }

}