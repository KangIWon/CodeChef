package com.sparta.codechef.domain.user.entity;

import com.sparta.codechef.common.Timestamped;
import com.sparta.codechef.common.enums.Organization;
import com.sparta.codechef.common.enums.UserRole;
import com.sparta.codechef.domain.chat.v1.entity.ChatRoom;
import jakarta.persistence.*;
import java.time.LocalDate;
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

    @Column(nullable = false)
    private String userName;

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

    @Column(name = "customer_key")
    private String customerKey; // 토스페이먼츠 고객 키

    @Builder.Default
    private boolean isPlusUser = false;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    public void isDelete() {
        isDeleted = true;
    }
    @Column
    private LocalDate lastAttendDate;

    public void updateLastAttendDate() {
        this.lastAttendDate = LocalDate.now();
    }

    // 출석 체크시,포인트 지급 메서드
    public void addPoint() {
        this.point += 100; // 포인트 증가
    }

    public void eventAddPoint() {
        this.point += 1000; // 포인트 증가
    }

    // 마지막 출석체크날로부터 일주일 지나면 포인트 10%감소한 값 바꿔주는 메서드
    public void updatePoint(Integer point) {
        this.point = point;
    }

    // 댓글 채택시, 댓글단 유저에게 포인트 지급 메서드
    public void addPointToCommentUser() {
        this.point += 200;
    }

    public void changeIsAttend() {
        this.isAttended = true;
    }

    public void isAttended() {
        isAttended = true;
    }

    public void changePassword(String password) {
        this.password = password;
    }

    public boolean isBlocked() {
        return blockUntil != null && blockUntil.isAfter(LocalDateTime.now());
    }

    public void addWarningAndSetBlock() {
        this.warning ++;
        if (this.warning.equals(1) ) {
            this.blockUntil = LocalDateTime.now().plusDays(warning);
        }
        if (this.warning.equals(2)) {
            this.blockUntil = LocalDateTime.now().minusDays(7);
        }
        if (this.warning > 2) {
            this.blockUntil = LocalDateTime.now().minusDays(30);
        }
    }

    public void updateChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }

    public String checkRedisKey(User user) {
             return user.getOrganization() == Organization.EMPLOYED
                ? "employed:ranking:realTime"
                : "unemployed:ranking:realTime";
    }

    public void saveCustomerKey(String customerKey) {
        this.customerKey = customerKey;
    }
}
