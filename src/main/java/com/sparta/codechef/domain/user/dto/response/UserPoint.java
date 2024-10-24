package com.sparta.codechef.domain.user.dto.response;

import lombok.Getter;

@Getter
public class UserPoint {
    private final Integer point;

    public UserPoint(Integer point) {
        this.point = point;
    }
}
