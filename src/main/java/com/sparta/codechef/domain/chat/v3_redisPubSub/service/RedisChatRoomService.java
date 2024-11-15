package com.sparta.codechef.domain.chat.v3_redisPubSub.service;

import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.enums.UserRole;
import com.sparta.codechef.common.exception.ApiException;
import com.sparta.codechef.domain.chat.v3_redisPubSub.dto.ChatRoomDto;
import com.sparta.codechef.domain.chat.v3_redisPubSub.dto.UnsubscribeDto;
import com.sparta.codechef.domain.chat.v3_redisPubSub.dto.request.ChatRoomCreateRequest;
import com.sparta.codechef.domain.chat.v3_redisPubSub.dto.request.ChatRoomUpdateRequest;
import com.sparta.codechef.domain.chat.v3_redisPubSub.dto.response.ChatRoomGetResponse;
import com.sparta.codechef.domain.chat.v3_redisPubSub.entity.ChatRoom;
import com.sparta.codechef.domain.chat.v3_redisPubSub.entity.ChatUser;
import com.sparta.codechef.domain.chat.v3_redisPubSub.enums.RedisKey;
import com.sparta.codechef.domain.chat.v3_redisPubSub.repository.RedisChatRepository;
import com.sparta.codechef.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.sparta.codechef.domain.chat.v3_redisPubSub.enums.RedisHashKey.ID;

@Service
@RequiredArgsConstructor
public class RedisChatRoomService {

    private final RedisChatRepository chatRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 채팅방 생성
     * @param userId : 방장 ID
     * @param request : 채팅방 생성 정보 DTO(채팅방 이름, 비밀번호, 최대 정원)
     * @return
     */
    public ChatRoomGetResponse createRoom(Long userId, ChatRoomCreateRequest request) {
        ChatUser chatUser = this.chatRepository.findChatUserById(userId);
        if (chatUser.getRoomId() != null) {
            throw new ApiException(ErrorStatus.ALREADY_IN_CHATROOM);
        }

        ChatRoom chatRoom = ChatRoom.of(
                new ChatRoomDto(
                        this.chatRepository.generateId(RedisKey.ID_CHAT_ROOM),
                        request.getTitle(),
                        request.getPassword() == null ? null : passwordEncoder.encode(request.getPassword()),
                        request.getMaxParticipants(),
                        0,
                        userId
                ));

        this.chatRepository.saveChatRoom(chatRoom);

        chatUser = chatUser.subscribeChatRoomAsHost(chatRoom.getId());
        this.chatRepository.saveChatUser(chatUser);

        return this.chatRepository.subscribeChatRoom(chatRoom.getId(), userId);
    }

    /**
     * 채팅방 전체 조회 (with 페이징)
     * @param page : 페이지 번호
     * @param size : 페이지 크기
     * @return
     */
    public Page<ChatRoomGetResponse> getChatRooms(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, ID.getHashKey()));
        return this.chatRepository.findAllChatRooms(pageable);
    }


    /**
     * 채팅방 정보 수정
     * @param roomId : 채팅방 id
     * @param request : 채팅방 이름, 비밀번호, 최대 정원 정보가 담긴 DTO
     * @return
     */
    public ChatRoomGetResponse updateChatRoom(Long roomId, ChatRoomUpdateRequest request) {
        ChatRoom chatRoom = this.chatRepository.findChatRoomById(roomId);

        chatRoom = chatRoom.updateRoomInfo(
                request.getTitle(),
                request.getPassword() == null ? null : passwordEncoder.encode(request.getPassword()),
                request.getMaxParticipants()
        );

        return this.chatRepository.saveChatRoom(chatRoom);
    }


    /**
     * 채팅방 입장(구독)
     * @param roomId : 채팅방 id
     * @param userId : 유저 id
     */
    public ChatRoomGetResponse subscribeChatRoom(Long roomId, Long userId, String password) {
        ChatUser chatUser = this.chatRepository.findChatUserById(userId);

        if (chatUser.getRoomId() != null) {
            throw new ApiException(ErrorStatus.ALREADY_IN_CHATROOM);
        }

        ChatRoom chatRoom = this.chatRepository.findChatRoomById(roomId);
        String chatRoomPassword = chatRoom.getPassword();

        if (chatRoomPassword != null) {
            boolean isSame = false;

            if (password != null) {
                isSame = passwordEncoder.matches(password, chatRoomPassword);
            }

            if (!isSame) {
                throw new ApiException(ErrorStatus.ACCESS_DENIED_NOT_CORRECT_PASSWORD);
            }
        }

        if (chatRoom.getMaxParticipants() > chatRoom.getCurParticipants()) {
            return this.chatRepository.subscribeChatRoom(roomId, userId);
        }

        chatUser = chatUser.subscribeChatRoom(roomId);
        this.chatRepository.saveChatUser(chatUser);

        throw new ApiException(ErrorStatus.ROOM_CAPACITY_EXCEEDED);
    }

    /**
     * 채팅방 퇴장(구독 취소)
     * @param roomId : 채팅방 id
     * @param userId : 유저 id
     */
    public UnsubscribeDto unsubscribeChatRoom(Long roomId, Long userId) {
        ChatUser chatUser = this.chatRepository.findChatUserById(userId);

        if (chatUser.getRoomId() == null || !Objects.equals(chatUser.getRoomId(), roomId)) {
            throw new ApiException(ErrorStatus.NOT_IN_CHATROOM);
        }

        return this.chatRepository.unsubscribeChatRoom(roomId, chatUser);
    }


    // 방장 권한 체크용
    public boolean hasAccess(AuthUser authUser) {
        boolean isAdmin = authUser.getUserRole().equals(UserRole.ROLE_ADMIN);

        if (!isAdmin) {
            boolean isHost = this.chatRepository.isHost(authUser.getUserId());

            if (!isHost) {
                throw new ApiException(ErrorStatus.NOT_CHATROOM_HOST);
            }
        }

        return true;
    }
}