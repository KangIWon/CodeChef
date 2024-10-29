package com.sparta.codechef.domain.chat.v2.entity;

import com.sparta.codechef.common.enums.UserRole;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.InvalidRequestException;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum WSChatUserRole {
    ROLE_ADMIN(Authority.ADMIN),
    ROLE_HOST(Authority.HOST),
    ROLE_USER(Authority.USER);

    private final String authority;

    public static WSChatUserRole of(String role) {
        return Arrays.stream(WSChatUserRole.values())
                .filter(userRole -> userRole.name().equalsIgnoreCase(role))
                .findFirst()
                .orElseThrow(() -> new InvalidRequestException("유효하지 않은 권한입니다."));
    }

    public static WSChatUserRole of(UserRole role) {
        return Arrays.stream(WSChatUserRole.values())
                .filter(userRole -> userRole.name().equalsIgnoreCase(role.name()))
                .findFirst()
                .orElseThrow(() -> new InvalidRequestException("유효하지 않은 권한입니다."));
    }


    public static class Authority {
        public static final String ADMIN = "ROLE_ADMIN";
        public static final String HOST = "ROLE_HOST";
        public static final String USER = "ROLE_USER";
    }
}
