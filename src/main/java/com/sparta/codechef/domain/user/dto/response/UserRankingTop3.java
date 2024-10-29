package com.sparta.codechef.domain.user.dto.response;

import lombok.Getter;

@Getter
public class UserRankingTop3 {

    private final Long userId;
    private final Integer point;

    public UserRankingTop3(Long userId, Integer point) {
        this.userId = userId;
        this.point = point;
    }
}

