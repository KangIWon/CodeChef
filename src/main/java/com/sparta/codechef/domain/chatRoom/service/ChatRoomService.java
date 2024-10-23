package com.sparta.codechef.domain.chatRoom.service;

import com.sparta.codechef.domain.chatRoom.dto.request.ChatRoomRequest;
import com.sparta.codechef.domain.chatRoom.dto.response.ChatRoomResponse;
import com.sparta.codechef.domain.chatRoom.repository.chat_room.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    /**
     * 채팅방 생성
     * @param request : 채팅방 이름, 비밀번호, 채팅방 최대 인원 정보가 담긴 DTO
     * @return
     */
    public ChatRoomResponse createRoom(ChatRoomRequest request) {
        return null;
    }

    /**
     * 채팅방 전체 조회 (with 페이징)
     * @param page : 페이지 번호
     * @param size : 페이지 크기
     * @return
     */
    public Page<ChatRoomResponse> getChatRooms(int page, int size) {
        return null;
    }

    /**
     * 채팅방 정보 수정
     * @param request : 채팅방 이름, 비밀번호, 채팅방 최대 인원 정보가 담긴 DTO
     * @return
     */
    public ChatRoomResponse updateChatRoom(ChatRoomRequest request) {
        return null;
    }

    /**
     * 채팅방 입장
     * @return
     */
    public ChatRoomResponse enterChatRoom() {
        return null;
    }

    /**
     *  채팅방 퇴장
     * @return
     */
    public ChatRoomResponse exitChatRoom() {
        return null;
    }
}
