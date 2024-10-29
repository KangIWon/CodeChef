package com.sparta.codechef.domain.chat.v1.entity;

import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.exception.ApiException;
import com.sparta.codechef.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;


@Getter
@Entity
@NoArgsConstructor
@Builder(builderClassName = "ChatRoomBuilder")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;
    private String password;
    private int maxParticipants;
    @Builder.Default
    private boolean isDeleted = false;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 방장

    public void updateRoomInfo(String title, String password, Integer maxParticipants) {
        if (title != null) {
            this.title = title;
        }

        this.password = password;

        if (maxParticipants != null) {
            this.maxParticipants = maxParticipants;
        }
    }

    public void updateHost(User newHost) {
        this.user = newHost;
    }

    public void delete() {
        this.isDeleted = true;
    }

    public static class ChatRoomBuilder {
        public ChatRoomBuilder id(Long id) {
            throw new ApiException(ErrorStatus.ID_CANNOT_BE_SET);
        }

        public ChatRoomBuilder isDeleted(boolean isDeleted) {
            throw new ApiException(ErrorStatus.IS_DELETED_CANNOT_BE_SET);
        }
    }
}
