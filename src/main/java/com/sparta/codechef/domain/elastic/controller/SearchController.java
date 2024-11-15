package com.sparta.codechef.domain.elastic.controller;


import com.sparta.codechef.common.ApiResponse;
import com.sparta.codechef.common.enums.Framework;
import com.sparta.codechef.common.enums.Language;
import com.sparta.codechef.domain.elastic.document.BoardDocument;
import com.sparta.codechef.domain.elastic.dto.TopTenSearchResponse;
import com.sparta.codechef.domain.elastic.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    // 제목으로 검색
    @GetMapping("/boards/title")
    public ApiResponse<Page<BoardDocument>> searchByTitle(@RequestParam String title, Pageable pageable) throws IOException {
        return ApiResponse.ok("검색 되었습니다.",searchService.searchByTitle(title, pageable));
    }

    // 내용으로 검색
    @GetMapping("/boards/contents")
    public ApiResponse<Page<BoardDocument>> searchByContents(@RequestParam String contents, Pageable pageable) throws IOException {
        return ApiResponse.ok("검색 되었습니다.",searchService.searchByContents(contents, pageable));
    }

    // 제목 + 내용으로 검색
    @GetMapping("/boards/title-contents")
    public ApiResponse<Page<BoardDocument>> searchByTitleAndContents(@RequestParam String keyword, Pageable pageable) throws IOException {
        return ApiResponse.ok("검색 되었습니다.",searchService.searchByTitleAndContents(keyword, pageable));
    }

    // 프레임워크로 검색
    @GetMapping("/boards/framework")
    public ApiResponse<Page<BoardDocument>> searchByFramework(@RequestParam Framework framework, Pageable pageable) throws IOException {
        return ApiResponse.ok("검색 되었습니다.",searchService.searchByFramework(framework,pageable));
    }

    // 언어로 검색
    @GetMapping("/boards/language")
    public ApiResponse<Page<BoardDocument>> searchByLanguage(@RequestParam Language language, Pageable pageable ) throws IOException {
        return ApiResponse.ok("검색 되었습니다.",searchService.searchByLanguage(language, pageable));
    }

    @GetMapping("/boards/top10")
    public ApiResponse<List<TopTenSearchResponse>> getTop10ByField(@RequestParam String field) throws IOException{
        return ApiResponse.ok("",searchService.getTop10ByField(field));
    }
}





