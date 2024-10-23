package com.sparta.codechef.domain.chat.service;

import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.exception.ApiException;
import com.sparta.codechef.domain.chat.dto.request.ChatRoomRequest;
import com.sparta.codechef.domain.chat.dto.response.ChatRoomResponse;
import com.sparta.codechef.domain.chat.entity.ChatRoom;
import com.sparta.codechef.domain.chat.repository.chat_room.ChatRoomRepository;
import com.sparta.codechef.domain.user.entity.User;
import com.sparta.codechef.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    /**
     * 채팅방 생성
     * @param userId : 유저 id
     * @param request : 채팅방 이름, 비밀번호, 최대 정원 정보가 담긴 DTO
     * @return
     */
    @Transactional
    public ChatRoomResponse createRoom(Long userId, ChatRoomRequest request) {
        User user = this.userRepository.findById(userId).orElseThrow(
                () -> new ApiException(ErrorStatus.NOT_FOUND_USER)
        );

        String title = request.getTitle();
        if (title == null || title.isEmpty()) {
            title = "채팅방";
        }

        Integer maxParticipants = request.getMaxParticipants();
        if (maxParticipants == null) {
            maxParticipants = 10;
        } else if (maxParticipants < 2 || maxParticipants > 100){
            throw new ApiException(ErrorStatus.BAD_REQUEST_MAX_PARTICIPANTS);
        }

        ChatRoom chatRoom = ChatRoom.builder()
                .title(title)
                .maxParticipants(maxParticipants)
                .password(request.getPassword())
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
    public Page<ChatRoomResponse> getChatRooms(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id"));

        Page<ChatRoom> chatRoomList = this.chatRoomRepository.findAll(pageable);

        return chatRoomList.map(ChatRoomResponse::new);
    }


    /**
     * 채팅방 정보 수정
     * @param userId : 유저 id
     * @param chatRoomId : 채팅방 id
     * @param request : 채팅방 이름, 비밀번호, 최대 정원 정보가 담긴 DTO
     * @return
     */
    @Transactional
    public ChatRoomResponse updateChatRoom(Long userId, Long chatRoomId, ChatRoomRequest request) {
        ChatRoom chatRoom = this.chatRoomRepository.findByIdAndUser(chatRoomId, userId).orElseThrow(
                () -> new ApiException(ErrorStatus.NOT_FOUND_CHATROOM)
        );

        chatRoom.updateRoomInfo(
                request.getTitle(),
                request.getPassword(),
                request.getMaxParticipants()
        );

        ChatRoom updatedChatRoom = this.chatRoomRepository.save(chatRoom);

        return new ChatRoomResponse(updatedChatRoom);
    }


    /**
     * 채팅방 입장
     * @param userId : 유저 id
     * @param chatRoomId : 채팅방 id
     */
    @Transactional
    public void enterChatRoom(Long userId, Long chatRoomId) {
        User user = this.userRepository.findById(userId).orElseThrow(
                () -> new ApiException(ErrorStatus.NOT_FOUND_USER)
        );

        if (user.getChatRoom() != null) {
            throw new ApiException(ErrorStatus.ALREADY_IN_CHATROOM);
        }

        ChatRoom chatRoom = this.chatRoomRepository.findById(chatRoomId).orElseThrow(
                () -> new ApiException(ErrorStatus.NOT_FOUND_CHATROOM)
        );

        int curParticipants = this.userRepository.countAllByChatRoom(chatRoomId);

        if (chatRoom.getMaxParticipants() > curParticipants) {
            user.updateChatRoom(chatRoom);
        } else {
            throw new ApiException(ErrorStatus.ROOM_CAPACITY_EXCEEDED);
        }
    }

    /**
     * 채팅방 퇴장
     * @param userId : 유저 id
     * @param chatRoomId : 채팅방 id
     * @return
     */
    @Transactional
    public void exitChatRoom(Long userId, Long chatRoomId) {
        User user = this.userRepository.findById(userId).orElseThrow(
                () -> new ApiException(ErrorStatus.NOT_FOUND_USER)
        );

        if (user.getChatRoom() == null) {
            throw new ApiException(ErrorStatus.NOT_IN_CHATROOM);
        }

        user.updateChatRoom(null);

        this.userRepository.flush();

        ChatRoom chatRoom = this.chatRoomRepository.findById(chatRoomId).orElseThrow(
                () -> new ApiException(ErrorStatus.NOT_FOUND_CHATROOM)
        );

        int curParticipants = this.userRepository.countAllByChatRoom(chatRoomId);

        if (curParticipants == 0) {
            this.chatRoomRepository.delete(chatRoom);
        }
    }
}
