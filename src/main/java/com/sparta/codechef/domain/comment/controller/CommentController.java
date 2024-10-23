//package com.sparta.codechef.domain.comment.controller;
//
//import com.sparta.codechef.common.ApiResponse;
//import com.sparta.codechef.domain.comment.dto.CommentRequest;
//import com.sparta.codechef.domain.comment.dto.CommentResponse;
//import com.sparta.codechef.domain.comment.dto.CommentUpdateResponse;
//import com.sparta.codechef.domain.comment.service.CommentService;
//import com.sparta.codechef.security.AuthUser;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@Controller
//@RequiredArgsConstructor
//@RequestMapping("/api")
//public class CommentController {
//
//    private final CommentService commentService;
//
//    @PostMapping("/boards/{boardId}/comments")
//    public ApiResponse createComment(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long boardId, @RequestBody CommentRequest commentRequest)
//    {
//        return ApiResponse.createSuccess(HttpStatus.OK.value(), "댓글이 생성되었습니다.",commentService.createComment(authUser,boardId,commentRequest));
//    }
//    @GetMapping("/comments")
//    public ApiResponse<List<CommentResponse>> getComments(@AuthenticationPrincipal AuthUser authUser)
//    {
//        return ApiResponse.createSuccess(HttpStatus.OK.value(), "댓글이 조회되었습니다.",commentService.getComments(authUser));
//    }
//    @PatchMapping("/boards/{boardId}/comments/{commentsId}")
//    public ApiResponse<CommentUpdateResponse> updateComment(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long boardId, @PathVariable Long commentsId, @RequestBody CommentRequest commentRequest)
//    {
//       return ApiResponse.createSuccess(HttpStatus.OK.value(),"댓글이 수정되었습니다.", commentService.updateComment(authUser, boardId, commentsId, commentRequest));
//    }
//    @DeleteMapping("/boards/{boardId}/comments/{commentId}")
//    public ApiResponse deleteComment(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long boardId, @PathVariable Long commentId)
//    {
//        return ApiResponse.createSuccess(HttpStatus.OK.value(), "댓글이 삭제되었습니다.", commentService.deleteComment(authUser, boardId, commentId));
//    }
//
//}
