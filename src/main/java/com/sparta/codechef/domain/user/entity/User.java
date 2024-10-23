package com.sparta.codechef.domain.user.entity;

import com.sparta.codechef.common.Timestamped;
import com.sparta.codechef.common.enums.Organization;
import com.sparta.codechef.common.enums.UserRole;
import com.sparta.codechef.domain.chat.entity.ChatRoom;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Getter
@Entity
@NoArgsConstructor
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "users")
public class User extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(length = 150, nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String personalHistory;

    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    @Enumerated(EnumType.STRING)
    private Organization organization;

    @Builder.Default
    private Integer warning = 0;

    @Builder.Default
    private Boolean isDeleted = false;

    @Builder.Default
    private Integer point = 0;

    @Builder.Default
    private Boolean isAttended = false;

    // 계정 차단 만료 시간
    @Column
    private LocalDateTime blockUntil;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    public void isDelete() {
        isDeleted = true;
    }

    public void isAttended() {
        isAttended = true;
    }

    public void changePassword(String password) {
        this.password = password;
    }

    public boolean isBlocked() {
        return blockUntil != null && blockUntil.isBefore(LocalDateTime.now());
    }

    public void addWarningAndSetBlock(int dayToBlock) {
        this.warning ++;
        this.blockUntil = LocalDateTime.now().plusDays(dayToBlock);
    }

    public void updateChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }
}
