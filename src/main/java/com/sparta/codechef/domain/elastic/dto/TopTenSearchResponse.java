package com.sparta.codechef.domain.elastic.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TopTenSearchResponse {
    private String keyword;
    private long searchCount;



    public TopTenSearchResponse(String keyword, long searchCount) {
        this.keyword = keyword;
        this.searchCount = searchCount;
    }

}
