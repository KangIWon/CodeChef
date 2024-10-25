package com.sparta.codechef.domain.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ChatRoomCreateRequest {

    @NotBlank(message = "채팅방 이름은 공백 문자가 될 수 없습니다.")
    private final String title;
    @NotBlank(message = "채팅방 비밀번호는 공백 문자가 될 수 없습니다.")
    private final String password;
    @Size(min = 2, max = 100, message = "채팅방 최대 정원은 2명 이상 100명 이하만 가능합니다.")
    private final Integer maxParticipants;

    public ChatRoomCreateRequest(String title, String password, Integer maxParticipants) {
        this.title = title;
        this.password = password;
        this.maxParticipants = maxParticipants == null ? 10 : maxParticipants;
    }
}
