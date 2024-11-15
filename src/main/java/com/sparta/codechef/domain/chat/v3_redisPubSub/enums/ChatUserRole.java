package com.sparta.codechef.domain.chat.v3_redisPubSub.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;
import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.enums.UserRole;
import com.sparta.codechef.common.exception.ApiException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

import static com.sparta.codechef.domain.chat.v3_redisPubSub.enums.ChatUserRole.Authority.*;

@Getter
@RequiredArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum ChatUserRole {
    ROLE_ADMIN(ADMIN),
    ROLE_HOST(HOST),
    ROLE_USER(USER);

    private final String authority;

    public static ChatUserRole of(String role) {
        return Arrays.stream(ChatUserRole.values())
                .filter(userRole -> userRole.name().equalsIgnoreCase(role))
                .findFirst()
                .orElseThrow(() -> new ApiException(ErrorStatus.INVALID_CHAT_USER_ROLE));
    }

    public static ChatUserRole of(UserRole role) {
        return Arrays.stream(ChatUserRole.values())
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