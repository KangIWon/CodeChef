package com.sparta.codechef.domain.board.controller;

import com.sparta.codechef.common.ApiResponse;
import com.sparta.codechef.domain.board.dto.request.BoardCreatedRequest;
import com.sparta.codechef.domain.board.dto.request.BoardModifiedRequest;
import com.sparta.codechef.domain.board.dto.response.BoardDetailResponse;
import com.sparta.codechef.domain.board.dto.response.BoardResponse;
import com.sparta.codechef.domain.board.service.BoardService;
import com.sparta.codechef.security.AuthUser;
import lombok.RequiredArgsConstructor;
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
    public ApiResponse<List<BoardResponse>> findAllBoard() {
        return ApiResponse.ok("게시물 전체 조회 성공", boardService.findAllBoard());
    }

    @GetMapping("/{boardId}") // 게시판 단건 조회
    public ApiResponse<BoardDetailResponse> getBoard(@PathVariable Long boardId,
                                                     AuthUser authUser) {
        return ApiResponse.ok(boardId +"번 게시물 조회", boardService.getBoard(boardId, authUser));
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

}
