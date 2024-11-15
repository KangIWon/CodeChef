package com.sparta.codechef.security;

import com.sparta.codechef.common.enums.UserRole;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;


import java.util.Collection;
import java.util.List;

@Getter
public class AuthUser {

    private final Long userId;
    private final String email;
    private final String userName;
    private final UserRole userRole;

    public AuthUser(Long userId, String email, String userName, UserRole userRole) {
        this.userId = userId;
        this.email = email;
        this.userName = userName;
        this.userRole = userRole;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(userRole.name()));
    }
}
