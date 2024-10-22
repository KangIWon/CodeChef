package com.sparta.codechef.domain.board.dto.response;

import com.sparta.codechef.common.enums.Framework;
import com.sparta.codechef.common.enums.Language;
import com.sparta.codechef.domain.comment.dto.CommentResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BoardResponse {

    private final Long id;
    private final Long user_id;
    private final String title;
    private final String content;
    private final Language language;
    private final Framework framework;
    private final List<CommentResponse> commentResponseDtoList;


    public BoardResponse(Long id,
                         Long user_id,
                         String title,
                         String content,
                         Language language,
                         Framework framework,
                         List<CommentResponse> commentResponseDtoList) {
        this.id = id;
        this.user_id = user_id;
        this.title = title;
        this.content = content;
        this.language = language;
        this.framework = framework;
        this.commentResponseDtoList = commentResponseDtoList;
    }


}
