package com.sparta.codechef.domain.chat.v3_redisPubSub.repository;

import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.exception.ApiException;
import com.sparta.codechef.domain.chat.v3_redisPubSub.dto.ChatRoomDto;
import com.sparta.codechef.domain.chat.v3_redisPubSub.dto.ChatUserDto;
import com.sparta.codechef.domain.chat.v3_redisPubSub.dto.MessageDto;
import com.sparta.codechef.domain.chat.v3_redisPubSub.dto.UnsubscribeDto;
import com.sparta.codechef.domain.chat.v3_redisPubSub.dto.response.ChatRoomGetResponse;
import com.sparta.codechef.domain.chat.v3_redisPubSub.dto.response.ChatUserGetResponse;
import com.sparta.codechef.domain.chat.v3_redisPubSub.entity.ChatMessage;
import com.sparta.codechef.domain.chat.v3_redisPubSub.entity.ChatRoom;
import com.sparta.codechef.domain.chat.v3_redisPubSub.entity.ChatUser;
import com.sparta.codechef.domain.chat.v3_redisPubSub.enums.ChatUserRole;
import com.sparta.codechef.domain.chat.v3_redisPubSub.enums.RedisKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import static com.sparta.codechef.domain.chat.v3_redisPubSub.enums.ChatUserRole.ROLE_HOST;
import static com.sparta.codechef.domain.chat.v3_redisPubSub.enums.RedisHashKey.*;
import static com.sparta.codechef.domain.chat.v3_redisPubSub.enums.RedisKey.*;

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
        this.redisTemplate.opsForList().rightPush(CHAT_USER_LIST.getKey(roomId),  userId);
        this.redisTemplate.opsForHash().increment(CHAT_ROOM.getKey(roomId), CUR_PARTICIPANTS.getHashKey(), 1);

        ChatRoomDto chatRoomDto = this.chatRoomRepository.findById(roomId).orElseThrow(
                () -> new ApiException(ErrorStatus.FAILED_TO_CREATE_CHAT_ROOM)
        );

        return new ChatRoomGetResponse(ChatRoom.of(chatRoomDto));
    }


    /**
     * 채팅방 퇴장(구독 해제)
     *   - 채팅방 현재 구독자 수 -1
     *   - 유저의 구독 채팅방 id 삭제
     *   - 구독자가 없으면 채팅방 삭제
     *   - 퇴장한 유저가 방장일 때, 승계할 유저 ID 반환
     *
     * @param roomId : 채팅방 ID
     * @param chatUser : 유저 객체
     */
    public UnsubscribeDto unsubscribeChatRoom(Long roomId, ChatUser chatUser) {
        Long userId = chatUser.getId();
        this.redisTemplate.opsForList().remove(CHAT_USER_LIST.getKey(roomId), 0, userId);
        this.redisTemplate.opsForHash().increment(CHAT_ROOM.getKey(roomId), CUR_PARTICIPANTS.getHashKey(),-1);

        boolean isHost = this.isHost(userId);
        chatUser = chatUser.unsubscribeChatRoom();
        this.saveChatUser(chatUser);

        Long hostId = null;
        if (isHost) {
            hostId = this.successChatRoomHost(roomId);
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

        chatRoomList = chatRoomList.stream().toList();
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
    private List<Long> findAllMessageIdByChatRoomId(Long roomId) {
        try {
            Double doubleRoomId = Double.valueOf(roomId);
            return Objects.requireNonNull(this.stringRedisTemplate.opsForZSet().rangeByScore(CHAT_ROOM_MESSAGE.getKey(), doubleRoomId, doubleRoomId))
                    .stream()
                    .filter(Objects::nonNull)
                    .mapToLong(Long::parseLong)
                    .boxed()
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
    public List<ChatMessage> findAllMessageByRoomId(Long roomId) {
        return this.findAllMessageDtoById(roomId).stream()
                .map(ChatMessage::fromMessageDto)
                .toList();
    }

    /**
     * 채팅방 ID로 채팅 메세지 전체 조회
     * @param roomId : 채팅방 ID
     * @return
     */
    private List<MessageDto> findAllMessageDtoById(Long roomId) {
        List<MessageDto> messageDtoList = new ArrayList<>();

        List<Long> messageIdList = this.findAllMessageIdByChatRoomId(roomId);

        if (messageIdList.isEmpty()) {
            return messageDtoList;
        }

        this.messageRepository.findAllById(messageIdList).forEach(messageDtoList::add);

        return messageDtoList.stream()
                .sorted((o1, o2) -> (int) (o1.getId() - o2.getId()))
                .toList();
    }

    /**
     * 채팅방 ID로 채팅 메세지 전체 조회
     * @param roomId : 채팅방 ID
     * @return
     */
    public List<MessageDto> getMessageGetResponseListByRoomId(Long roomId){
        return this.findAllMessageDtoById(roomId).stream().toList();
    }

    /**
     * 메세지 전체 조회 with 페이징
     * @param roomId : 채팅방 Id
     * @param pageable : 페이징 정보
     * @return
     */
    public Page<MessageDto> findAllMessageByRoomId(Long roomId, Pageable pageable) {
        List<MessageDto> messageList = this.getMessageGetResponseListByRoomId(roomId);
        long totalMessage = messageList.size();
        int end = (int) totalMessage - Integer.parseInt(String.valueOf(pageable.getOffset()));
        int start = end - Integer.parseInt(String.valueOf(pageable.getPageSize()));
        start = Math.max(start, 0);

        if (end < 0 || totalMessage == 0) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }

        List<MessageDto> messageDtoList = IntStream.range(start, end)
                .mapToObj(messageList::get)
                .toList();

        return new PageImpl<>(messageDtoList, pageable, totalMessage);
    }

    /**
     * 메세지 저장
     * @param message : 메세지
     */
    public MessageDto saveMessage(ChatMessage message) {
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
     * 방장 ID Getter
     * @param roomId : 채팅방 ID
     * @return
     */
    private Long getHostId(Long roomId) {
        List<String> nextHostIdList = this.stringRedisTemplate.opsForList().range(CHAT_USER_LIST.getKey(roomId), 0, 0);

        if (nextHostIdList == null || nextHostIdList.isEmpty()) {
            return null;
        }

        return Long.parseLong(nextHostIdList.get(0));
    }

    /**
     * 채팅방 방장 승계할 유저 ID getter
     * @param roomId : 채팅방 ID
     */
    public Long successChatRoomHost(Long roomId) {
        Long nextHostId = this.getHostId(roomId);
        ChatUser nextHost = this.findChatUserById(nextHostId);
        nextHost = nextHost.updateRoleAsHOST();
        this.saveChatUser(nextHost);

        ChatRoom chatRoom = this.findChatRoomById(roomId);
        chatRoom = chatRoom.updateHost(nextHostId);
        this.saveChatRoom(chatRoom);

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


    /**
     * 채팅방 전체 유저 조회
     * @param roomId : 채팅방 ID
     * @return
     */
    public List<ChatUserGetResponse> findAllChatUserListByRoomId(Long roomId) {
        try {
            List<Long> chatRoomUserIdList = Objects.requireNonNull(this.stringRedisTemplate.opsForList().range(CHAT_USER_LIST.getKey(roomId), 0, -1))
                    .stream()
                    .mapToLong(Long::parseLong)
                    .boxed()
                    .toList();

            List<ChatUserGetResponse> chatUserList = new ArrayList<>();
            this.chatUserRepository.findAllById(chatRoomUserIdList)
                    .forEach(chatUserDto -> chatUserList.add(
                            new ChatUserGetResponse(
                                    chatUserDto.getId().toString(),
                                    chatUserDto.getEmail()
                            )));

            return chatUserList;
        } catch (NullPointerException e) {
            throw new ApiException(ErrorStatus.CHATROOM_IS_EMPTY);
        }
    }


    /**
     * 채팅방 전체 유저 조회 with 페이징
     * @param roomId : 채팅방 ID
     * @param pageable : 페이징 정보
     * @return
     */
    public Page<ChatUserGetResponse> findAllChatUserByRoomId(Long roomId, Pageable pageable) {
        List<ChatUserGetResponse> chatUserList = this.findAllChatUserListByRoomId(roomId);
        long totalUsers = chatUserList.size();
        int end = (int) totalUsers - Integer.parseInt(String.valueOf(pageable.getOffset())) - 1;
        int start = end - Integer.parseInt(String.valueOf(pageable.getPageSize()));
        start = Math.min(start, 0);

        if (end < 0 || totalUsers == 0) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }

        List<ChatUserGetResponse> chatUserGetResponseList = IntStream.range(start, end)
                .mapToObj(chatUserList::get)
                .toList();

        return new PageImpl<>(chatUserGetResponseList, pageable, totalUsers);

    }


}