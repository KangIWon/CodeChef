package com.sparta.codechef.domain.attachment.controller;

import com.sparta.codechef.common.ApiResponse;
import com.sparta.codechef.domain.attachment.dto.response.AttachmentResponse;
import com.sparta.codechef.domain.attachment.service.AttachmentService;
import com.sparta.codechef.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boards/{boardId}/attachments")
public class AttachmentController {

    private final AttachmentService attachmentService;

    /**
     * 첨부파일 저장
     * @param authUser : 인증 유저
     * @param boardId : 게시물 ID
     * @param fileList : 저장할 첨부파일 리스트
     * @return 상태 코드, 상태 메세지, 첨부파일 정보(파일 이름, URL)
     */
    @PostMapping
    public ApiResponse<List<AttachmentResponse>> uploadFiles(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long boardId,
            @RequestPart(name = "file") List<MultipartFile> fileList
    ) {
        return ApiResponse.ok(
                "첨부파일이 추가되었습니다.",
                this.attachmentService.uploadFiles(authUser.getUserId(), boardId, fileList)
        );
    }

    /**
     * 첨부파일 다건 조회 by 게시물 ID
     *
     * @param boardId : 게시글 ID
     * @return 상태 코드, 상태 메세지, 첨부파일 정보 리스트
     */
    @GetMapping
    public ApiResponse<List<AttachmentResponse>> getFiles(@PathVariable Long boardId) {
        return ApiResponse.ok(
                "첨부파일 목록 전체가 조회되었습니다.",
                this.attachmentService.getFiles(boardId)
        );
    }


    /**
     * 첨부파일 수정
     * @param boardId : 게시글 ID
     * @param file : 첨부파일 리스트
     * @return 상태 코드, 상태 메세지, 첨부파일 정보 리스트
     */
    @PutMapping
    public ApiResponse<List<AttachmentResponse>> getFiles(
            @RequestBody Long boardId,
            @RequestPart(name = "file") List<MultipartFile> file
    ) {
        return ApiResponse.ok(
                "첨부파일이 수정되었습니다.",
                this.attachmentService.updateFiles(boardId, file)
        );
    }
}
