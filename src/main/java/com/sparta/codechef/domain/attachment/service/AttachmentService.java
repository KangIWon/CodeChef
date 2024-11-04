package com.sparta.codechef.domain.attachment.service;

import com.amazonaws.services.s3.AmazonS3;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final BoardRepository boardRepository;
    private final AmazonS3 amazonS3;

    @Value("${s3.bucket}")
    private String S3_BUCKET;

    @Value("${cloudfront.url}")
    private String CLOUD_FRONT_URL;

    private final Long MAX_FILE_SIZE = 5 * 1024 * 1024L;  // 단일 파일 최대 용량 : 5MB
    private final Long MAX_REQUEST_SIZE = 10 * 1024 * 1024L;  // 전체 업로드 파일 최대 용량 : 10MB

    /**
     * 첨부파일 추가
     * @param boardId : 게시글 ID
     * @param fileList : 첨부파일 리스트
     * @return 첨부파일 정보 리스트 (파일명, URL)
     */
    public List<AttachmentResponse> uploadFiles(Long boardId, List<MultipartFile> fileList) {
        this.validateAttachmentFiles(fileList);
        this.deleteFiles(boardId);

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

        return this.getKeyListFromS3(boardId).stream().map(key ->
                new AttachmentResponse(
                    this.getOriginalFileName(boardId, key),
                    this.getCdnUrl(key)
                )
        ).toList();
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
                .withBucketName(S3_BUCKET)
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
            amazonS3.putObject(S3_BUCKET, s3Key, file.getInputStream(), metadata);

        } catch (IOException e) {
            throw new ApiException(ErrorStatus.FAILED_TO_UPLOAD_ATTACHMENT);
        }

        return new AttachmentResponse(
                file.getOriginalFilename(),
                this.getCdnUrl(s3Key)
        );
    }

    /**
     * 단일 첨부 파일 삭제
     * @param key : 첨부 파일
     */
    public void deleteFile(String key) {
        try {
            amazonS3.deleteObject(S3_BUCKET, key);
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
                .append("board")
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
        if (originalFileName.length() > 25) {
            originalFileName = originalFileName.substring(0, 26);
        }

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


    // S3 URL 경로 CloudFront URL 변환 메서드
    private String getCdnUrl(String key) {
        return new StringBuffer()
                .append(CLOUD_FRONT_URL)
                .append("/")
                .append(key)
                .toString();
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

        if (!isAdmin) {
            boolean isWriter = this.boardRepository.existsByIdAndUserId(authUser.getUserId(), boardId);

            if (!isWriter) {
                throw new ApiException(ErrorStatus.NOT_BOARD_WRITER);
            }
        }

        return true;
    }

    private void validateAttachmentFiles(List<MultipartFile> fileList) {
        fileList.stream().filter(file -> file.getSize() > MAX_FILE_SIZE).findFirst().ifPresent(file -> {
            throw new ApiException(ErrorStatus.MAX_UPLOAD_FILE_SIZE_EXCEEDED);
        });

        long totalSize = fileList.stream().mapToLong(MultipartFile::getSize).sum();

        if (totalSize == 0) {
            throw new ApiException(ErrorStatus.EMPTY_ATTACHMENT_LIST);
        }

        if (totalSize > MAX_REQUEST_SIZE) {
            throw new ApiException(ErrorStatus.MAX_UPLOAD_REQUEST_SIZE_EXCEEDED);
        }

        long distinctFileNames = fileList.stream().map(MultipartFile::getOriginalFilename).distinct().count();

        if (distinctFileNames != fileList.size()) {
            throw new ApiException(ErrorStatus.NOT_UNIQUE_FILENAME);
        }


        boolean isEmptyAttachmentList = fileList.stream()
                .filter(Objects::nonNull)
                .map(MultipartFile::getOriginalFilename)
                .filter(Objects::nonNull)
                .filter(fileName -> !fileName.isBlank())
                .toList().isEmpty();

        if (isEmptyAttachmentList) {
            throw new ApiException(ErrorStatus.EMPTY_ATTACHMENT_LIST);
        }
    }
}
