package com.sparta.codechef.domain.chat.entity;

import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.Timestamped;
import com.sparta.codechef.common.exception.ApiException;
import com.sparta.codechef.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor
@Builder(builderClassName = "MessageBuilder")
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Message extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String message;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public static class MessageBuilder {
        public MessageBuilder id(Long id) {
            throw new ApiException(ErrorStatus.ID_CANNOT_BE_SET);
        }

        public MessageBuilder createdAt(LocalDateTime createdAt) {
            throw new ApiException(ErrorStatus.CREATED_AT_CANNOT_BE_SET);
        }
    }
}
