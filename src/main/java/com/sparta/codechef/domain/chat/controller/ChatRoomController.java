package com.sparta.codechef.domain.chat.controller;

import com.sparta.codechef.common.ApiResponse;
import com.sparta.codechef.domain.chat.dto.request.ChatRoomRequest;
import com.sparta.codechef.domain.chat.dto.response.ChatRoomResponse;
import com.sparta.codechef.domain.chat.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatRooms")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    /**
     * 채팅방 생성
     * @param request : 채팅방 이름, 비밀번호, 최대 인원
     * @return 채팅방 ID, 이름, 비공개 여부, 최대 인원
     */
    @PostMapping
    public ApiResponse<ChatRoomResponse> createChatRoom(ChatRoomRequest request) {
        return new ApiResponse<>(HttpStatus.OK.value(),
                "채팅방이 생성되었습니다.",
                this.chatRoomService.createRoom(request)
        );
    }


    /**
     * 채팅방 전체 조회
     * @param page : 페이지 번호
     * @param size : 페이지 크기
     * @return 페이징 된 채팅방 정보 리스트
     */
    @GetMapping
    public ApiResponse<Page<ChatRoomResponse>> getChatRooms(@RequestParam int page, @RequestParam int size) {
        return ApiResponse.ok(
                "채팅방 전체 목록을 조회하였습니다.",
                this.chatRoomService.getChatRooms(page, size)
        );
    }


    /**
     * 채팅방 정보 수정
     * @param request : 채팅방 이름, 비밀번호, 최대 인원
     * @return 채팅방 ID, 이름, 비공개 여부, 최대 인원
     */
    @PutMapping("/{chatRoomId}")
    public ApiResponse<ChatRoomResponse> updateChatRoom(
            ChatRoomRequest request
    ) {
        return ApiResponse.ok(
                "채팅방이 수정되었습니다.",
                this.chatRoomService.updateChatRoom(request)
        );
    }

    /**
     * 채팅방 입장
     * @return
     */
    @PostMapping("/{chatRoomId}")
    public ApiResponse<ChatRoomResponse> enterChatRoom() {
        return ApiResponse.ok(
                "채팅방에 입장하셨습니다.",
                this.chatRoomService.enterChatRoom()
        );
    }

    @DeleteMapping("/{chatRoomId}")
    public ApiResponse<ChatRoomResponse> exitChatRoom() {
        return ApiResponse.ok(
                "채팅방에서 퇴장하셨습니다.",
                this.chatRoomService.exitChatRoom()
        );
    }
}
