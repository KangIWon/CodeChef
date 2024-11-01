package com.sparta.codechef.domain.attachment.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.enums.UserRole;
import com.sparta.codechef.common.exception.ApiException;
import com.sparta.codechef.domain.attachment.dto.response.AttachmentResponse;
import com.sparta.codechef.domain.board.repository.BoardRepository;
import com.sparta.codechef.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final BoardRepository boardRepository;
    private final AmazonS3 amazonS3;

    @Value("${s3.bucket}")
    private String bucketName;

    /**
     * 첨부파일 추가
     * @param boardId : 게시글 ID
     * @param fileList : 첨부파일 리스트
     * @return 첨부파일 정보 리스트 (파일명, URL)
     */
    public List<AttachmentResponse> uploadFiles(Long boardId, List<MultipartFile> fileList) {
        if (fileList.isEmpty()) {
            throw new ApiException(ErrorStatus.EMPTY_ATTACHMENT_LIST);
        }

        this.getKeyListFromS3(boardId).forEach(this::deleteFile);

        return fileList.stream().map(file -> this.uploadFile(boardId, file)).toList();
    }


    /**
     * 게시글에 첨부된 첨부파일 조회
     * @param boardId : 게시글 ID
     * @return 첨부파일 정보 리스트(파일명, URL)
     */
    public List<AttachmentResponse> getFiles(Long boardId) {
        boolean isPresentBoard = this.boardRepository.existsById(boardId);

        if (!isPresentBoard) {
            throw new ApiException(ErrorStatus.NOT_FOUND_BOARD);
        };

        return this.getKeyListFromS3(boardId).stream().map(key -> {
            String s3Url = amazonS3.getUrl(bucketName, key).toString();

            return new AttachmentResponse(
                    this.getOriginalFileName(boardId, key),
                    s3Url
            );
        }).toList();
    }


    /**
     * 게시글 첨부파일 전체 삭제
     * @param boardId : 게시글 ID
     */
    public void deleteFiles(Long boardId) {
        this.getKeyListFromS3(boardId).forEach(this::deleteFile);
    }


    // S3 요청 메서드
    /**
     * S3에 업로드된 게시물의 첨부파일 key 리스트 조회
     * @param boardId : 게시물 ID
     * @return key 리스트
     */
    public List<String> getKeyListFromS3(Long boardId) {
        ListObjectsV2Request request = new ListObjectsV2Request()
                .withBucketName(bucketName)
                .withPrefix(this.getPath(boardId));

        ListObjectsV2Result result = amazonS3.listObjectsV2(request);

        return result.getObjectSummaries().stream()
                .map(S3ObjectSummary::getKey)
                .toList();
    }

    /**
     * 단일 첨부파일 업로드
     * @param boardId : 게시글 ID
     * @param file : 첨부파일
     * @return 첨부파일 정보(파일명, URL)
     */
    public AttachmentResponse uploadFile(Long boardId, MultipartFile file) {
        String s3Key = this.getS3Key(boardId, file.getOriginalFilename());

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        try {
            amazonS3.putObject(bucketName, s3Key, file.getInputStream(), metadata);

        } catch (IOException e) {
            throw new ApiException(ErrorStatus.FAILED_TO_UPLOAD_ATTACHMENT);
        }

        return new AttachmentResponse(
                file.getOriginalFilename(),
                amazonS3.getUrl(bucketName, s3Key).toString()
        );
    }

    /**
     * 단일 첨부 파일 삭제
     * @param key : 첨부 파일
     */
    public void deleteFile(String key) {
        try {
            amazonS3.deleteObject(bucketName, key);
        } catch (Exception e) {
            throw new ApiException(ErrorStatus.FAILED_TO_DELETE_ATTACHMENT);
        }
    }


    // GETTER 메서드
    /**
     * S3 파일 저장 경로 Getter
     * @param boardId : 게시물 ID
     * @return "/board/{boardId}/"
     */
    private String getPath(Long boardId) {
        return new StringBuffer()
                .append("/board")
                .append(boardId)
                .append("/")
                .toString();
    }


    /**
     * S3 버킷 저장 경로/파일.확장자 Getter
     * @param boardId : 게시물 ID
     * @param originalFileName : 파일 이름.확장자
     * @return "/board/{boardId}/{파일명}/{확장자}
     */
    private String getS3Key(Long boardId, String originalFileName) {
        return new StringBuffer()
                .append(this.getPath(boardId))
                .append(originalFileName)
                .toString();
    }

    /**
     * 조회한 key에서 파일명.확장자 Getter
     * @param boardId : 게시물 ID
     * @param key : S3 파일 저장 경로/파일명.확장자
     * @return "{파일명}.{확장자}"
     */
    private String getOriginalFileName(Long boardId, String key) {
        return key.substring(this.getPath(boardId).length());
    }


    // @AuthWriter 에서 사용하는 메서드
    /**
     * 게시물의 작성자 여부 조회
     * @param authUser : 인증 유저
     * @param boardId : 게시물 ID
     * @return
     */
    public boolean hasAccess(AuthUser authUser, Long boardId) {
        boolean isAdmin = authUser.getUserRole().equals(UserRole.ROLE_ADMIN);

        if (isAdmin) {
            boolean isWriter = this.boardRepository.existsByIdAndUserId(authUser.getUserId(), boardId);

            if (!isWriter) {
                throw new ApiException(ErrorStatus.NOT_BOARD_WRITER);
            }
        }

        return true;
    }
}
