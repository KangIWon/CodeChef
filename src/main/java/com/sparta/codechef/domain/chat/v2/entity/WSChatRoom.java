package com.sparta.codechef.domain.chat.v2.entity;

import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.exception.ApiException;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;


@Getter
@Builder
@RedisHash("ChatRoom")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WSChatRoom implements Serializable {

    @Id
    private Long id;
    @NotNull
    private String title;
    private String password;
    private int maxParticipants;
    @NotNull
    private Long hostId;  // 방장


    public void updateRoomInfo(String title, String password, Integer maxParticipants) {
        if (title != null) {
            this.title = title;
        }

        this.password = password;

        if (maxParticipants != null) {
            this.maxParticipants = maxParticipants;
        }
    }

    public void updateHost(Long hostId) {
        this.hostId = hostId;
    }
}
