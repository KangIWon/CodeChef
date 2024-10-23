package com.sparta.codechef.domain.attachment.controller;

import com.sparta.codechef.common.ApiResponse;
import com.sparta.codechef.domain.attachment.dto.response.AttachmentResponse;
import com.sparta.codechef.domain.attachment.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/attachments")
public class AttachmentController {

    private final AttachmentService attachmentService;

    /**
     * 첨부파일 추가
     *
     * @param file : 첨부 파일
     * @return 상태 코드, 상태 메세지, 첨부파일 정보
     */
    @PostMapping
    public ApiResponse<List<AttachmentResponse>> uploadFiles(@RequestPart(name = "file") List<MultipartFile> file) {
        return new ApiResponse<>(HttpStatus.OK.value(),
                "첨부파일이 추가되었습니다.",
                this.attachmentService.uploadFiles(file)
        );
    }

    /**
     * 게시글에 첨부된 첨부파일 조회
     *
     * @param boardId : 게시글 ID
     * @return 상태 코드, 상태 메세지, 첨부파일 정보 리스트
     */
    @GetMapping
    public ApiResponse<List<AttachmentResponse>> getFiles(@RequestBody Long boardId) {
        return new ApiResponse<>(HttpStatus.OK.value(),
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
        return new ApiResponse<>(HttpStatus.OK.value(),
                "첨부파일이 수정되었습니다.",
                this.attachmentService.updateFiles(boardId, file)
        );
    }
}
