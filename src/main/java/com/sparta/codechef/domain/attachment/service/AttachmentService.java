package com.sparta.codechef.domain.attachment.service;

import com.sparta.codechef.domain.attachment.dto.response.AttachmentResponse;
import com.sparta.codechef.domain.attachment.repository.AttachmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;

    /**
     * 첨부파일 추가
     * @param file : 첨부 파일
     * @return 첨부파일 정보 리스트
     */
    public List<AttachmentResponse> uploadFiles(List<MultipartFile> file) {
        return null;
    }

    /**
     * 게시글에 첨부된 첨부파일 조회
     * @param boardId : 게시글 ID
     * @return 첨부파일 정보 리스트
     */
    public List<AttachmentResponse> getFiles(Long boardId) {
        return null;
    }

    /**
     * 첨부파일 수정
     * @param boardId : 게시글 ID
     * @param file : 첨부 파일 리스트
     * @return 첨부파일 정보 리스트
     */
    public List<AttachmentResponse> updateFiles(Long boardId, List<MultipartFile> file) {
        return null;
    }
}
