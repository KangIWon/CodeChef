package com.sparta.codechef.domain.chat.v2.entity;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("chatRoom")
public class WSChatRoom implements Serializable {

    @Id
    private Long id;
    @NotNull
    private String title;
    private String password;
    private int maxParticipants;
    private int curParticipants;
    @NotNull
    private Long hostId;  // 방장

    public void updateRoomInfo(String title, String password, int maxParticipants) {
        if (title != null) {
            this.title = title;
        }

        this.password = password;

        if (maxParticipants != 0) {
            this.maxParticipants = maxParticipants;
        }
    }
}
