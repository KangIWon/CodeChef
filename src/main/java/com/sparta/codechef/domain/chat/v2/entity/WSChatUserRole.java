package com.sparta.codechef.domain.chat.v2.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;
import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.enums.UserRole;
import com.sparta.codechef.common.exception.ApiException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum WSChatUserRole {
    ROLE_ADMIN(Authority.ADMIN),
    ROLE_HOST(Authority.HOST),
    ROLE_USER(Authority.USER);

    private final String authority;

    public static WSChatUserRole of(String role) {
        return Arrays.stream(WSChatUserRole.values())
                .filter(userRole -> userRole.name().equalsIgnoreCase(role))
                .findFirst()
                .orElseThrow(() -> new ApiException(ErrorStatus.INVALID_CHAT_USER_ROLE));
    }

    public static WSChatUserRole of(UserRole role) {
        return Arrays.stream(WSChatUserRole.values())
                .filter(userRole -> userRole.name().equalsIgnoreCase(role.name()))
                .findFirst()
                .orElseThrow(() -> new ApiException(ErrorStatus.INVALID_CHAT_USER_ROLE));
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }

    public static class Authority {
        public static final String ADMIN = "ROLE_ADMIN";
        public static final String HOST = "ROLE_HOST";
        public static final String USER = "ROLE_USER";
    }
}
