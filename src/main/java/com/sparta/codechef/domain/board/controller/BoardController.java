package com.sparta.codechef.domain.board.controller;

import com.sparta.codechef.common.ApiResponse;
import com.sparta.codechef.domain.board.dto.request.BoardCreatedRequest;
import com.sparta.codechef.domain.board.dto.request.BoardModifiedRequest;
import com.sparta.codechef.domain.board.dto.response.BoardDetailResponse;
import com.sparta.codechef.domain.board.dto.response.BoardResponse;
import com.sparta.codechef.domain.board.service.BoardService;
import com.sparta.codechef.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/board")
public class BoardController {

    private final BoardService boardService;

    @PostMapping// 게시판 생성
    public ApiResponse createBoard(@RequestPart BoardCreatedRequest request,
                                   AuthUser authUser) {

        return ApiResponse.createSuccess(HttpStatus.OK.value(), "게시글 작성되었습니다.", boardService.createBoard(request, authUser));
    }

    @GetMapping// 게시판 전체 조회
    public ApiResponse<Page<BoardResponse>> findAllBoard(@RequestParam(defaultValue = "1") int page,
                                                         @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok("게시물 전체 조회 성공", boardService.findAllBoard(page, size));
    }

    @GetMapping("/{boardId}") // 게시판 단건 조회
    public ApiResponse<BoardDetailResponse> getBoard(@PathVariable Long boardId,
                                                     AuthUser authUser) {
        return ApiResponse.ok(boardId +"번 게시물 조회", boardService.getBoard(boardId, authUser));
    }

    @GetMapping ("/myboard/{userId}")// 자기 게시물만 보기
    public ApiResponse<Page<BoardResponse>> myCreatedBoard(AuthUser authUser,
                                                           @PathVariable Long userId,
                                                           @RequestParam(defaultValue = "1") int page,
                                                           @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.onSuccess("", boardService.myCreatedBoard(authUser, userId, page, size));
    }

    @PutMapping("/{boardId}") // 게시물 수정
    public ApiResponse modifiedBoard(@PathVariable Long boardId,
                                     @RequestPart BoardModifiedRequest request,
                                     AuthUser authUser) {
        return ApiResponse.onSuccess(boardId +"번 게시물 수정",boardService.modifiedBoard(boardId, request, authUser));
    }

    @DeleteMapping("/{boardId}") // 게시물 삭제
    public ApiResponse deletedBoard(@PathVariable Long boardId,
                                    @RequestParam Long userId,
                                    AuthUser authUser) {
        return ApiResponse.onSuccess(boardId +"번 게시물 삭제", boardService.deletedBoard(boardId, authUser, userId));
    }

    @GetMapping("/search") // 게시물 제목, 내용으로 검색
    public ApiResponse<Page<BoardResponse>> boardSearch(
                                                        @RequestParam(required = false) String title,
                                                        @RequestParam(required = false) String content,
                                                        @RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.onSuccess("검색 기록", boardService.boardSearch(title,content,page,size));
    }
}
