package com.sparta.codechef.domain.chat.v2.service;

import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.enums.UserRole;
import com.sparta.codechef.common.exception.ApiException;
import com.sparta.codechef.domain.chat.v1.dto.request.ChatRoomCreateRequest;
import com.sparta.codechef.domain.chat.v1.dto.request.ChatRoomRequest;
import com.sparta.codechef.domain.chat.v1.dto.response.ChatRoomGetResponse;
import com.sparta.codechef.domain.chat.v1.dto.response.ChatRoomResponse;
import com.sparta.codechef.domain.chat.v2.entity.WSChatRoom;
import com.sparta.codechef.domain.chat.v2.entity.WSChatUser;
import com.sparta.codechef.domain.chat.v2.entity.WSChatUserRole;
import com.sparta.codechef.domain.chat.v2.repository.WSChatRepository;
import com.sparta.codechef.domain.chat.v2.repository.WSChatRoomRepository;
import com.sparta.codechef.domain.chat.v2.repository.WSChatUserRepository;
import com.sparta.codechef.security.AuthUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class WSChatService {

    private final WSChatRepository chatRepository;
    private final WSChatRoomRepository chatRoomRepository;
    private final WSChatUserRepository chatUserRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 채팅방 생성
     * @param userId : 방장 ID
     * @param request : 채팅방 생성 정보 DTO(채팅방 이름, 비밀번호, 최대 정원)
     * @return
     */
    public ChatRoomResponse createRoom(Long userId, ChatRoomCreateRequest request) {
        WSChatUser chatUser = this.chatUserRepository.findById(userId).orElseThrow(
                () -> new ApiException(ErrorStatus.NOT_FOUND_CHAT_USER)
        );

        if (chatUser.getRoomId() != null) {
            throw new ApiException(ErrorStatus.ALREADY_IN_CHATROOM);
        }

        WSChatRoom chatRoom = new WSChatRoom(
                this.chatRepository.generateId("chatRoom"),
                request.getTitle(),
                request.getPassword() == null ? null : passwordEncoder.encode(request.getPassword()),
                request.getMaxParticipants(),
                0,
                userId
        );

        Long roomId = chatRoom.getId();
        chatUser.updateRole(WSChatUserRole.ROLE_HOST);
        chatUser.updateChatRoom(roomId);
        this.chatUserRepository.save(chatUser);
        this.chatRoomRepository.save(chatRoom);
        this.chatRepository.subscribeChatRoom(chatRoom.getId(), userId);
        return new ChatRoomResponse(chatRoom);
    }

    /**
     * 채팅방 전체 조회 (with 페이징)
     * @param page : 페이지 번호
     * @param size : 페이지 크기
     * @return
     */
    public Page<ChatRoomGetResponse> getChatRooms(int page, int size) {
        return this.chatRepository.findAllChatRooms(page, size);
    }


    /**
     * 채팅방 정보 수정
     * @param chatRoomId : 채팅방 id
     * @param request : 채팅방 이름, 비밀번호, 최대 정원 정보가 담긴 DTO
     * @return
     */
    public ChatRoomResponse updateChatRoom(Long chatRoomId, ChatRoomRequest request) {
        WSChatRoom chatRoom = this.chatRoomRepository.findById(chatRoomId).orElseThrow(
                () -> new ApiException(ErrorStatus.NOT_FOUND_CHATROOM)
        );

        chatRoom.updateRoomInfo(
                request.getTitle(),
                request.getPassword() == null ? null : passwordEncoder.encode(request.getPassword()),
                request.getMaxParticipants()
        );

        this.chatRoomRepository.save(chatRoom);
        return new ChatRoomResponse(chatRoom);
    }


    /**
     * 채팅방 입장(구독)
     * @param roomId : 채팅방 id
     * @param userId : 유저 id
     */
    public void subscribeChatRoom(Long roomId, Long userId, String password) {
        log.info("입장");
        WSChatUser chatUser = this.chatUserRepository.findById(userId).orElseThrow(
                () -> new ApiException(ErrorStatus.NOT_FOUND_CHAT_USER)
        );

        if (chatUser.getRoomId() != null) {
            throw new ApiException(ErrorStatus.ALREADY_IN_CHATROOM);
        }
        log.info("채팅방 조회");
        WSChatRoom chatRoom = this.chatRoomRepository.findById(roomId).orElseThrow(
                () -> new ApiException(ErrorStatus.NOT_FOUND_CHATROOM)
        );

        String chatRoomPassword = chatRoom.getPassword();

        log.info("비밀번호 체크");
        if (chatRoomPassword != null) {
            boolean isSame = false;
            log.info("isSame: {}", isSame);
            if (password != null) {
                isSame = passwordEncoder.matches(password, chatRoomPassword);
            }
            log.info("isSame: {}", isSame);
            if (!isSame) {
                throw new ApiException(ErrorStatus.ACCESS_DENIED_NOT_CORRECT_PASSWORD);
            }
        }

        if (chatRoom.getMaxParticipants() > chatRoom.getCurParticipants()) {
            this.chatRepository.subscribeChatRoom(roomId, userId);
        } else {
            throw new ApiException(ErrorStatus.ROOM_CAPACITY_EXCEEDED);
        }
    }

    /**
     * 채팅방 퇴장(구독 취소)
     * @param roomId : 채팅방 id
     * @param userId : 유저 id
     */
    public void unsubscribeChatRoom(Long roomId, Long userId) {
        WSChatUser chatUser = this.chatUserRepository.findById(userId).orElseThrow(
                () -> new ApiException(ErrorStatus.NOT_FOUND_CHAT_USER)
        );

        if (chatUser.getRoomId() == null || !Objects.equals(chatUser.getRoomId(), roomId)) {
            throw new ApiException(ErrorStatus.NOT_IN_CHATROOM);
        }

        this.chatRepository.unsubscribeChatRoom(roomId, userId);
    }


    // 웹 소켓 연결 후, 채팅 유저 저장
    public void connectChatUser(WSChatUser chatUser) {
        this.chatUserRepository.save(chatUser);
    }

    // 웹 소켓 연결 해제 후, 채팅 유저 삭제
    public void disconnectChatUser(Long userId) {
        this.chatUserRepository.deleteById(userId);
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
