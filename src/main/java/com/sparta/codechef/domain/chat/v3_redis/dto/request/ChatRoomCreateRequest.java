package com.sparta.codechef.domain.chat.v3_redis.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class ChatRoomCreateRequest {

    @Pattern(regexp = ".*\\S.*", message = "채팅방 이름은 공백 문자가 될 수 없습니다.")
    private final String title;
    @Pattern(regexp = ".*\\S.*", message = "채팅방 비밀번호는 공백 문자가 될 수 없습니다.")
    private final String password;
    @Min(value = 2, message = "채팅방 최대 정원은 2명 이상이어야 합니다.")
    @Max(value = 100, message = "채팅방 최대 정원은 100명 이하이어야 합니다.")
    private final int maxParticipants;

    public ChatRoomCreateRequest(String title, String password, int maxParticipants) {
        this.title = title == null ? "Code Chef 채팅방" : title;
        this.password = password;
        this.maxParticipants = maxParticipants == 0 ? 10 : maxParticipants;
    }
}
