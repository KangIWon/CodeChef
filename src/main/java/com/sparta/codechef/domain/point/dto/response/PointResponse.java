package com.sparta.codechef.domain.point.dto.response;

import lombok.Getter;

@Getter
public class PointResponse {

    private final Integer balance;

    public PointResponse(Integer balance) {
        this.balance = balance;
    }
}
