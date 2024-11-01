package com.sparta.codechef.domain.board.dto.request;

import com.sparta.codechef.security.AuthUser;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BoardDetailEvent {
    private final AuthUser authUser;
    private final Long boardId;
}
