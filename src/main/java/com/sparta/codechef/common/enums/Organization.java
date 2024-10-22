package com.sparta.codechef.common.enums;

import com.sun.jdi.request.InvalidRequestStateException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum Organization {
    EMPLOYED,
    UNEMPLOYED;

    public static Organization of(String type) {
        return Arrays.stream(Organization.values())
                .filter(r -> r.name().equalsIgnoreCase(type))
                .findFirst()
                .orElseThrow(() -> new InvalidRequestStateException("유효하지 않은 Organization 입니다."));
    }
}
