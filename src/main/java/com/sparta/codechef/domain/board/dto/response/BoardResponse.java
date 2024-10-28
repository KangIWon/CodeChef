package com.sparta.codechef.domain.board.dto.response;

import com.sparta.codechef.common.enums.Framework;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BoardResponse {

    private Long id;
    private  Long userId;
    private  String title;
    private  String content;
    private  String language;
    private  Framework framework;


    public BoardResponse(Long id,
                Long userId,
                String title,
                String content,
                String language,
                Framework framework) {
            this.id = id;
            this.userId = userId;
            this.title = title;
            this.content = content;
            this.language = language;
            this.framework = framework;
    }

}
