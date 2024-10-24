package com.sparta.codechef.domain.chat.entity;

import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.exception.ApiException;
import com.sparta.codechef.domain.chat.dto.request.ChatRoomRequest;
import com.sparta.codechef.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Getter
@Entity
@NoArgsConstructor
@Builder(builderClassName = "ChatRoomBuilder")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String password;
    private int maxParticipants;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user; // 방장

    public void updateRoomInfo(String title, String password, Integer maxParticipants) {
        if (title != null && !title.isBlank()) {
            this.title = title;
        }

        if (password != null && !password.isBlank()) {
            this.password = password;
        }

        if (maxParticipants != null) {
            if (maxParticipants < 2 || maxParticipants > 100) {
                throw new ApiException(ErrorStatus.BAD_REQUEST_MAX_PARTICIPANTS);
            }

            this.maxParticipants = maxParticipants;
        }
    }

    public static class ChatRoomBuilder {
        public ChatRoomBuilder id(Long id) {
            throw new ApiException(ErrorStatus.ID_CANNOT_BE_SET);
        }
    }
}
