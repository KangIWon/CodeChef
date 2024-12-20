package com.sparta.codechef.domain.attachment.controller;

import com.sparta.codechef.common.ApiResponse;
import com.sparta.codechef.common.annotation.AuthForBoard;
import com.sparta.codechef.domain.attachment.dto.response.AttachmentResponse;
import com.sparta.codechef.domain.attachment.service.AttachmentService;
import lombok.RequiredArgsConstructor;
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
     * @param boardId : 게시물 ID
     * @param fileList : 저장할 첨부파일 리스트
     * @return 상태 코드, 상태 메세지, 첨부파일 정보(파일 이름, URL)
     */
    @AuthForBoard
    @PostMapping
    public ApiResponse<List<AttachmentResponse>> uploadFiles(
            @PathVariable Long boardId,
            @RequestPart(name = "fileList") List<MultipartFile> fileList
    ) {
        return ApiResponse.ok(
                "첨부파일이 추가되었습니다.",
                this.attachmentService.uploadFiles(boardId, fileList)
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
     * 게시물 전체 첨부파일 삭제
     * @param boardId
     * @return
     */
    @AuthForBoard
    @DeleteMapping
    public ApiResponse<Void> deleteFiles(@PathVariable Long boardId) {
        this.attachmentService.deleteFiles(boardId);

        return ApiResponse.ok("게시물의 첨부파일이 전부 삭제되었습니다.", null);
    }
}
