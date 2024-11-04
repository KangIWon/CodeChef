package com.sparta.codechef.domain.board.controller;

import com.sparta.codechef.common.ApiResponse;
import com.sparta.codechef.common.annotation.AuthForBoard;
import com.sparta.codechef.domain.board.dto.request.BoardCreatedRequest;
import com.sparta.codechef.domain.board.dto.request.BoardModifiedRequest;
import com.sparta.codechef.domain.board.dto.response.BoardDetailResponse;
import com.sparta.codechef.domain.board.dto.response.BoardResponse;
import com.sparta.codechef.domain.board.service.BoardService;

import com.sparta.codechef.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boards")
public class BoardController {

    private final BoardService boardService;

    /**
     * 게시물 작성
     * @param authUser : 로그인 유저 정보
     * @param request : 게시판 생성에 필요한 request
     * */
    @PostMapping// 게시판 생성
    public ApiResponse<Void> createBoard(@RequestBody BoardCreatedRequest request,
                                   @AuthenticationPrincipal AuthUser authUser) {

        return ApiResponse.ok("게시글 작성되었습니다.", boardService.createBoard(request, authUser));
    }

    /**
     *  전체 게시물 보기
     * @param page : 10번 페이지 중 현재 페이지 번호
     * @param size : 한 페이지에 보여줄 게시물 수
     * */
    @GetMapping// 게시판 전체 조회
    public ApiResponse<Page<BoardResponse>> findAllBoard(@RequestParam(defaultValue = "1") int page,
                                                         @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok("게시물 전체 조회 성공", boardService.findAllBoard(page, size));
    }

    /**
     * 자기가 쓴 게시물 보기
     * @param authUser : 로그인 유저 정보
     * @param page : 10번 페이지 중 현재 페이지 번호
     * @param size : 한 페이지에 보여줄 게시물 수
     * */
    @GetMapping ("/myboard")// 자기 게시물만 보기
    public ApiResponse<Page<BoardResponse>> myCreatedBoard(@AuthenticationPrincipal AuthUser authUser,
                                                           @RequestParam(defaultValue = "1") int page,
                                                           @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok("내가 쓴 게시물", boardService.myCreatedBoard(authUser, page, size));
    }

    /**
     * 게시물 수정
     * @param authUser : 로그인 유저 정보
     * @param request : 게시판 수정에 필요한 request
     * @param boardId : 수정 하려는 게시물 번호
     * */
    @AuthForBoard
    @PutMapping("/{boardId}") // 게시물 수정
    public ApiResponse modifiedBoard(@PathVariable Long boardId,
                                     @RequestBody BoardModifiedRequest request,
                                     @AuthenticationPrincipal AuthUser authUser) {
        return ApiResponse.ok(boardId +"번 게시물 수정",boardService.modifiedBoard(boardId, request, authUser));
    }

    /**
     * 게시물 삭제
     * @param authUser : 로그인 유저 정보
     * @param boardId : 수정 하려는 게시물 번호
     **/
    @AuthForBoard
    @DeleteMapping("/{boardId}") // 게시물 삭제
    public ApiResponse deletedBoard(@PathVariable Long boardId,
                                    @AuthenticationPrincipal AuthUser authUser) {
        return ApiResponse.ok(boardId +"번 게시물 삭제", boardService.deletedBoard(boardId, authUser));
    }

    /**
     * 게시물 검색
     * @param  title : 제목으로 검색
     * @param  content : 내용으로 검색
     * @param  page : 10번 페이지 중 현재 페이지 번호
     * @param  size : 한 페이지에 보여줄 게시물 수
     **/
    @GetMapping("/search") // 게시물 제목, 내용으로 검색
    public ApiResponse<Page<BoardResponse>> boardSearch(
                                                        @RequestParam(required = false) String title,
                                                        @RequestParam(required = false) String content,
                                                        @RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok("검색 기록", boardService.boardSearch(title,content,page,size));
    }

    /**
     * 게시물 단건 조회
     * @param boardId : 보려고 하는 게시물 번호
     * */
    // 보드 조회 (조회수 카운팅 포함)
    @GetMapping("/{boardId}")
    public ApiResponse<BoardDetailResponse> getBoardDetails(@AuthenticationPrincipal AuthUser authUser,
                                                            @PathVariable Long boardId) {
        // 사용자 ID를 활용해 어뷰징 방지를 위한 조회수 카운팅
        return ApiResponse.ok(boardId +"번 게시물 조회", boardService.getBoardDetails(authUser, boardId));
    }

    // 실시간 인기 보드 랭킹 조회
    @GetMapping("/top")
    public ApiResponse<List<BoardResponse>> getTopBoards() {
        return ApiResponse.ok( "실시간으로 가장 인기 있는 보드를 조회했습니다.", boardService.getTopBoards());
    }
}
