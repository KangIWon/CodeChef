package com.sparta.codechef.domain.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentResponse {
    private Long id;
    private Long userId;
    private Long boardId;
    private String comment;
    private Boolean isAdopted;

    public CommentResponse(Long id, String comment, Long userid, Long boardId, Boolean isAdopted) {
        this.id = id;
        this.comment = comment;
        this.userId = userid;
        this.boardId = boardId;
        this.isAdopted = isAdopted;
    }
}
