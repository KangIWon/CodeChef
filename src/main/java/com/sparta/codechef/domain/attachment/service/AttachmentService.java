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
import com.sparta.codechef.domain.attachment.entity.Attachment;
import com.sparta.codechef.domain.attachment.repository.AttachmentRepository;
import com.sparta.codechef.domain.board.entity.Board;
import com.sparta.codechef.domain.board.repository.BoardRepository;
import com.sparta.codechef.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final BoardRepository boardRepository;
    private final AttachmentRepository attachmentRepository;
    private final AmazonS3 amazonS3;

    @Value("${s3.bucket}")
    private String S3_BUCKET;

    @Value("${cloudfront.url}")
    private String CLOUD_FRONT_URL;

    private final List<String> ALLOWED_MIME_TYPES = List.of("image/jpeg", "image/png");
    private final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png");

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

        Board board = this.boardRepository.findById(boardId).orElseThrow(
                () -> new ApiException(ErrorStatus.NOT_FOUND_BOARD)
        );

        return fileList.stream().map(file -> this.uploadFile(board, file)).toList();
    }


    /**
     * 게시글에 첨부된 첨부파일 조회
     * @param boardId : 게시글 ID
     * @return 첨부파일 정보 리스트(파일명, cloudFrontFileURL)
     */
    public List<AttachmentResponse> getFiles(Long boardId) {
        boolean isPresentBoard = this.boardRepository.existsById(boardId);

        if (!isPresentBoard) {
            throw new ApiException(ErrorStatus.NOT_FOUND_BOARD);
        };

        List<Attachment> attachmentList = this.attachmentRepository.findAllByBoardId(boardId);

        if (attachmentList != null && !attachmentList.isEmpty()) {
            return attachmentList.stream().map(attachment ->
                new AttachmentResponse(attachment.getId(), attachment.getS3Key(), attachment.getCdnUrl())
            ).toList();
        }

        return new ArrayList<>();
    }


    /**
     * 게시글 첨부파일 전체 삭제
     * @param boardId : 게시글 ID
     */
    public void deleteFiles(Long boardId) {
        this.attachmentRepository.findAllByBoardId(boardId)
                .forEach(attachment -> this.attachmentRepository.deleteById(attachment.getId()));

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
     * @param board : 게시글 엔티티
     * @param file : 첨부파일
     * @return 첨부파일 정보(파일명, cloudFrontFileURL)
     */
    public AttachmentResponse uploadFile(Board board, MultipartFile file) {
        String s3Key = this.getS3Key(board.getId(), file.getOriginalFilename());

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        try {
            amazonS3.putObject(S3_BUCKET, s3Key, file.getInputStream(), metadata);

        } catch (IOException e) {
            throw new ApiException(ErrorStatus.FAILED_TO_UPLOAD_ATTACHMENT);
        }

        Attachment attachment = Attachment.builder()
                .board(board)
                .s3Key(s3Key)
                .cdnUrl(this.getCdnUrl(s3Key))
                .build();

        Attachment savedAttachment = this.attachmentRepository.save(attachment);

        return new AttachmentResponse(savedAttachment);
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
        if (fileList.isEmpty()) {
            throw new ApiException(ErrorStatus.EMPTY_ATTACHMENT_LIST);
        }

        long totalSize = fileList.stream().filter(this::validateFile).mapToLong(MultipartFile::getSize).sum();
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
    }

    private boolean validateFile(MultipartFile file) {
        if (file == null) {
            throw new ApiException(ErrorStatus.FILE_IS_NULL);
        }

        // Mime Type 확인
        String mimeType = file.getContentType();
        if (!ALLOWED_MIME_TYPES.contains(mimeType)) {
            throw new ApiException(ErrorStatus.INVALID_MIME_TYPE);
        }

        // 개별 파일 사이즈 확인
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ApiException(ErrorStatus.MAX_UPLOAD_FILE_SIZE_EXCEEDED);
        }

        String fileName = file.getOriginalFilename();

        // 확장자 확인
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ApiException(ErrorStatus.INVALID_EXTENSION);
        }

        if (fileName == null) {
            throw new ApiException(ErrorStatus.FILE_NAME_IS_NULL);
        }

        if (fileName.isBlank()) {
            throw new ApiException(ErrorStatus.FILE_NAME_IS_EMPTY);
        }

        if (fileName.length() > 25) {
            throw new ApiException(ErrorStatus.FILE_NAME_IS_TOO_LONG);
        }

        return true;
    }
}
