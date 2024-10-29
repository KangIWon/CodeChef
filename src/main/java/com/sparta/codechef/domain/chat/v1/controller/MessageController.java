package com.sparta.codechef.domain.chat.v1.controller;

import com.sparta.codechef.common.ApiResponse;
import com.sparta.codechef.domain.chat.v1.dto.request.MessageRequest;
import com.sparta.codechef.domain.chat.v1.dto.response.MessageResponse;
import com.sparta.codechef.domain.chat.v1.service.MessageService;
import com.sparta.codechef.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatRooms/{chatRoomId}")
public class MessageController {
    //

    private final MessageService messageService;

    /**
     * 채팅 메세지 전송
     * @param authUser : 인증 유저
     * @param chatRoomId : 채팅방 ID
     * @param request : 채팅 메세지
     * @return
     */
    @PostMapping("/messages")
    public ApiResponse<MessageResponse> sendMessage(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long chatRoomId,
            @RequestBody MessageRequest request
    ) {
        return ApiResponse.ok(
                "채팅글을 전송하였습니다.",
                this.messageService.sendMessage(authUser.getUserId(), chatRoomId, request.getMessage())
        );
    }

    /**
     * 채팅방 메세지 다건 조회
     * @param authUser : 인증 유저
     * @param chatRoomId : 채팅방 ID
     * @return
     */
    @GetMapping
    public ApiResponse<List<MessageResponse>> getMessages(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long chatRoomId
    ) {
        return ApiResponse.ok(
                "채팅방 게시글이 조회되었습니다.",
                this.messageService.getMessages(authUser.getUserId(), chatRoomId)
        );
    }
}
