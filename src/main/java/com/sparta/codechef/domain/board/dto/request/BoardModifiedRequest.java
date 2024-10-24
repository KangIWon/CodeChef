package com.sparta.codechef.domain.board.dto.request;

import com.sparta.codechef.common.enums.Framework;
import com.sparta.codechef.common.enums.Language;
import lombok.Getter;

@Getter
public class BoardModifiedRequest {

    public String title;
    public String contents;
    public Language language;
    public Framework framework;

}
