package com.sparta.codechef.domain.user.entity;

import com.sparta.codechef.common.Timestamped;
import com.sparta.codechef.common.enums.Framework;
import com.sparta.codechef.common.enums.Organization;
import com.sparta.codechef.common.enums.UserRole;
import com.sparta.codechef.common.enums.Language;
import com.sparta.codechef.domain.point.entity.Point;
import jakarta.persistence.*;
import lombok.*;

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
    private Language language;

    @Enumerated(EnumType.STRING)
    private Framework framework;

    @Enumerated(EnumType.STRING)
    private Organization organization;

    @Column(nullable = false)
    private Integer warning;

    @Column(nullable = false)
    private Boolean isDeleted = false;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "point_id")
    private Point point;

    public void isDelete() {
        isDeleted = true;
    }
}
