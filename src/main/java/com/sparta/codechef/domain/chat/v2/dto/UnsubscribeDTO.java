package com.sparta.codechef.domain.chat.v2.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UnsubscribeDTO {
    private final boolean success;
    private final Long nextHostId;

}
