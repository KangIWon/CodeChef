package com.sparta.codechef.domain.chat.controller;

import com.sparta.codechef.common.ApiResponse;
import com.sparta.codechef.domain.chat.dto.response.MessageResponse;
import com.sparta.codechef.domain.chat.service.MessageService;
import lombok.RequiredArgsConstructor;
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
        return ApiResponse.ok(
                "채팅글을 전송하였습니다.",
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
        return ApiResponse.ok(
                "채팅방 게시글이 조회되었습니다.",
                this.messageService.getMessages(chatRoomId)
        );
    }
}
