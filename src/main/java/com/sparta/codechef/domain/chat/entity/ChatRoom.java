package com.sparta.codechef.domain.chat.entity;

import com.sparta.codechef.domain.chat.dto.request.ChatRoomRequest;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;


@Getter
@Entity
@NoArgsConstructor
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String password;
    private int maxParticipants;


    @Builder
    public ChatRoom(String title, String password, int maxParticipants) {
        this.title = title;
        this.password = password;
        this.maxParticipants = maxParticipants;
    }

    public void updateRoomInfo(String title, String password, int maxParticipants) {
        if (title != null && !title.isEmpty()) {
            this.title = title;
        }

        if (password != null && !password.isEmpty()) {
            this.password = password;
        }

        if (maxParticipants <= 100 & maxParticipants > 1) {
            this.maxParticipants = maxParticipants;
        }
    }


}
