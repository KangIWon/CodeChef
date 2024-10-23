package com.sparta.codechef.domain.chatRoom.controller;

import com.sparta.codechef.common.ApiResponse;
import com.sparta.codechef.domain.chatRoom.dto.response.MessageResponse;
import com.sparta.codechef.domain.chatRoom.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatRooms/{chatRoomId}")
public class MessageController {

    private final MessageService messageService;

    /**
     * 채팅 메세지 전송
     * @param message : 채팅 메세지
     * @return
     */
    @PostMapping("/messages")
    public ApiResponse<MessageResponse> sendMessage(@RequestBody String message) {
        return ApiResponse.onSuccess(
                this.messageService.sendMessage(message)
        );
    }

    /**
     * 채팅방 메세지 다건 조회
     * @param chatRoomId : 채팅방 ID
     * @return
     */
    @GetMapping
    public ApiResponse<List<MessageResponse>> getMesages(@PathVariable Long chatRoomId) {
        return ApiResponse.onSuccess(
                this.messageService.getMessages(chatRoomId)
        );
    }
}
