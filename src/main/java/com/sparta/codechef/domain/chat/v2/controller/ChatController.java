package com.sparta.codechef.domain.chat.v2.controller;

import com.sparta.codechef.common.ApiResponse;
import com.sparta.codechef.domain.chat.v1.dto.request.ChatRoomPasswordRequest;
import com.sparta.codechef.domain.chat.v2.annotation.AuthHost;
import com.sparta.codechef.domain.chat.v1.dto.request.ChatRoomCreateRequest;
import com.sparta.codechef.domain.chat.v1.dto.request.ChatRoomRequest;
import com.sparta.codechef.domain.chat.v1.dto.response.ChatRoomGetResponse;
import com.sparta.codechef.domain.chat.v1.dto.response.ChatRoomResponse;
import com.sparta.codechef.domain.chat.v2.dto.UnsubscribeDTO;
import com.sparta.codechef.domain.chat.v2.service.WSChatService;
import com.sparta.codechef.security.AuthUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/chats/rooms")
public class ChatController {

    private final WSChatService wsChatService;

    /**
     * 채팅방 생성
     * @param authUser : 인증 유저
     * @param request : 채팅방 이름, 비밀번호, 최대 인원
     */
    @PostMapping
    public ApiResponse<ChatRoomResponse> createChatRoom(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody ChatRoomCreateRequest request
    ) {
        return ApiResponse.ok(
                "채팅방이 생성되었습니다.",
                this.wsChatService.createRoom(authUser.getUserId(), request)
        );
    }

    /**
     * 채팅방 전체 조회
     * @param page : 페이지 번호
     * @param size : 페이지 크기
     * @return 페이징 된 채팅방 정보 리스트
     */
    @GetMapping
    public ApiResponse<Page<ChatRoomGetResponse>> getChatRooms(
            @RequestParam(defaultValue = "1")
            @Min(value = 1, message = "페이지 번호는 최소 1 이어야 합니다.")
            int page,
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "페이지 크기는 최소 1 이어야 합니다.")
            int size
    ) {
        return ApiResponse.ok(
                "채팅방 전체 목록을 조회하였습니다.",
                this.wsChatService.getChatRooms(page, size)
        );
    }


    /**
     * 채팅방 정보 수정
     * @param roomId : 채팅방 ID
     * @param request : 채팅방 이름, 비밀번호, 최대 인원
     * @return 채팅방 ID, 이름, 비공개 여부, 최대 인원
     */
    @AuthHost
    @PutMapping("/{roomId}")
    public ApiResponse<ChatRoomResponse> updateChatRoom(
            @PathVariable Long roomId,
            @RequestBody ChatRoomRequest request) {
        return ApiResponse.ok(
                "채팅방이 수정되었습니다.",
                this.wsChatService.updateChatRoom(roomId, request)
        );
    }


    /**
     * 채팅방 입장
     * @param authUser : 인증 유저
     * @param roomId : 채팅방 ID
     */
    @PostMapping("/{roomId}")
    public ApiResponse<Void> enterChatRoom(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long roomId,
            @RequestBody(required = false) ChatRoomPasswordRequest request
    ) {
        this.wsChatService.subscribeChatRoom(roomId, authUser.getUserId(), request.getPassword());

        return ApiResponse.ok(
                "채팅방에 입장하셨습니다.",
                null
        );
    }

    // 채팅방 유저 조회

    /**
     * 채팅방 퇴장
     * @param authUser : 인증 유저
     * @param roomId : 채팅방 ID
     */
    @DeleteMapping("/{roomId}")
    public ApiResponse<UnsubscribeDTO> exitChatRoom(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long roomId
    ) {
        return ApiResponse.ok(
                "채팅방에서 퇴장하셨습니다.",
                this.wsChatService.unsubscribeChatRoom(roomId, authUser.getUserId())
        );
    }
}
