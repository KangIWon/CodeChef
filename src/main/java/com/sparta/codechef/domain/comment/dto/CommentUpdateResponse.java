package com.sparta.codechef.domain.comment.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentUpdateResponse {

    private String comment;

    public CommentUpdateResponse(String comment)
    {
        this.comment = comment;

    }

}
