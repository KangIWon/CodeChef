package com.sparta.codechef.domain.user.entity;

import com.sparta.codechef.domain.framework.entity.Framework;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class UserFramework {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "framework_id")
    private Framework framework;

    public UserFramework(User user, Framework framework)
    {
        this.user = user;
        this.framework = framework;
    }
    public void update(Framework framework)
    {
        this.framework = framework;
    }
}
