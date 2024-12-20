package com.sparta.codechef.domain.chat.v1.service;

import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.enums.UserRole;
import com.sparta.codechef.common.exception.ApiException;
import com.sparta.codechef.domain.chat.v1.dto.request.ChatRoomCreateRequest;
import com.sparta.codechef.domain.chat.v1.dto.request.ChatRoomRequest;
import com.sparta.codechef.domain.chat.v1.dto.response.ChatRoomGetResponse;
import com.sparta.codechef.domain.chat.v1.dto.response.ChatRoomResponse;
import com.sparta.codechef.domain.chat.v1.entity.ChatRoom;
import com.sparta.codechef.domain.chat.v1.entity.Message;
import com.sparta.codechef.domain.chat.v1.repository.chat_room.ChatRoomRepository;
import com.sparta.codechef.domain.chat.v1.repository.message.MessageRepository;
import com.sparta.codechef.domain.user.entity.User;
import com.sparta.codechef.domain.user.repository.UserRepository;
import com.sparta.codechef.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 채팅방 생성
     * @param userId : 유저 id
     * @param request : 채팅방 이름, 비밀번호, 최대 정원 정보가 담긴 DTO
     * @return
     */
    @Transactional
    public ChatRoomResponse createRoom(Long userId, ChatRoomCreateRequest request) {
        User user = this.userRepository.findById(userId).orElseThrow(
                () -> new ApiException(ErrorStatus.NOT_FOUND_USER)
        );

        ChatRoom chatRoom = ChatRoom.builder()
                .title(request.getTitle())
                .maxParticipants(request.getMaxParticipants())
                .password(request.getPassword() == null ? null : passwordEncoder.encode(request.getPassword()))
                .user(user)
                .build();

        ChatRoom savedChatRoom = this.chatRoomRepository.save(chatRoom);
        user.updateChatRoom(savedChatRoom);

        return new ChatRoomResponse(savedChatRoom);
    }

    /**
     * 채팅방 전체 조회 (with 페이징)
     * @param page : 페이지 번호
     * @param size : 페이지 크기
     * @return
     */
    public Page<ChatRoomGetResponse> getChatRooms(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id"));

        return this.chatRoomRepository.findAllChatRoom(pageable);
    }


    /**
     * 채팅방 정보 수정
     * @param chatRoomId : 채팅방 id
     * @param request : 채팅방 이름, 비밀번호, 최대 정원 정보가 담긴 DTO
     * @return
     */
    @Transactional
    public ChatRoomResponse updateChatRoom(Long chatRoomId, ChatRoomRequest request) {
        ChatRoom chatRoom = this.chatRoomRepository.findExistChatRoomById(chatRoomId).orElseThrow(
                () -> new ApiException(ErrorStatus.NOT_FOUND_CHATROOM)
        );

        chatRoom.updateRoomInfo(
                request.getTitle(),
                request.getPassword() == null ? null : passwordEncoder.encode(request.getPassword()),
                request.getMaxParticipants()
        );

        ChatRoom updatedChatRoom = this.chatRoomRepository.save(chatRoom);

        return new ChatRoomResponse(updatedChatRoom);
    }


    /**
     * 채팅방 입장
     * @param chatRoomId : 채팅방 id
     * @param userId : 유저 id
     */
    @Transactional
    public void enterChatRoom(Long chatRoomId, Long userId, String password) {
        User user = this.userRepository.findById(userId).orElseThrow(
                () -> new ApiException(ErrorStatus.NOT_FOUND_USER)
        );

        if (user.getChatRoom() != null) {
            throw new ApiException(ErrorStatus.ALREADY_IN_CHATROOM);
        }

        ChatRoom chatRoom = this.chatRoomRepository.findExistChatRoomById(chatRoomId).orElseThrow(
                () -> new ApiException(ErrorStatus.NOT_FOUND_CHATROOM)
        );

        if (chatRoom.getPassword() != null) {
            boolean isSame = false;
            if (password != null) {
                isSame = passwordEncoder.matches(password, chatRoom.getPassword());
            }

            if (!isSame) {
                throw new ApiException(ErrorStatus.ACCESS_DENIED_NOT_CORRECT_PASSWORD);
            }
        }

        int curParticipants = this.userRepository.countAllByChatRoom(chatRoomId);

        if (chatRoom.getMaxParticipants() > curParticipants) {
            user.updateChatRoom(chatRoom);
        } else {
            throw new ApiException(ErrorStatus.ROOM_CAPACITY_EXCEEDED);
        }
    }

    /**
     * 채팅방 퇴장
     * @param chatRoomId : 채팅방 id
     * @param userId : 유저 id
     */
    @Transactional
    public void exitChatRoom(Long chatRoomId, Long userId) {
        User user = this.userRepository.findChatRoomUser(userId, chatRoomId).orElseThrow(
                () -> new ApiException(ErrorStatus.NOT_IN_CHATROOM)
        );

        ChatRoom chatRoom = this.chatRoomRepository.findExistChatRoomById(chatRoomId).orElseThrow(
                () -> new ApiException(ErrorStatus.NOT_FOUND_CHATROOM)
        );

        boolean isHost = this.chatRoomRepository.existsByIdAndUserId(chatRoomId, userId);

        if (isHost) {
            Optional<User> nextHost = this.userRepository.findNextHost(chatRoomId, userId);

            if (nextHost.isPresent()) {
                chatRoom.updateHost(nextHost.get());
            } else {
                chatRoom.delete();
                this.messageRepository.findAllMessagesByChatRoomId(chatRoomId).forEach(Message::delete);
            }
        }

        user.updateChatRoom(null);
    }

    // 방장 권한 체크용
    public boolean hasAccess(AuthUser authUser, Long chatRoomId) {
        boolean isAdmin = authUser.getUserRole().equals(UserRole.ROLE_ADMIN);

        if (!isAdmin) {
            boolean isHost = this.chatRoomRepository.existsByIdAndUserId(chatRoomId, authUser.getUserId());

            if (!isHost) {
                throw new ApiException(ErrorStatus.NOT_CHATROOM_HOST);
            }
        }

        return true;
    }
}
