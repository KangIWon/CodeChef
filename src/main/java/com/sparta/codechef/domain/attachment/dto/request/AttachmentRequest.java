package com.sparta.codechef.domain.attachment.dto.request;

import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.exception.ApiException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class AttachmentRequest {
    private final Long MAX_FILE_SIZE = 5 * 1024 * 1024L;  // 단일 파일 최대 용량 : 5MB
    private final Long MAX_REQUEST_SIZE = 10 * 1024 * 1024L;  // 전체 업로드 파일 최대 용량 : 10MB
    private final List<MultipartFile> files;

    public AttachmentRequest(List<MultipartFile> files) {
        files.stream().filter(file -> file.getSize() > MAX_FILE_SIZE).findFirst().ifPresent(file -> {
            throw new ApiException(ErrorStatus.MAX_UPLOAD_FILE_SIZE_EXCEEDED);
        });

        long totalSize = files.stream().mapToLong(MultipartFile::getSize).sum();

        if (totalSize > MAX_REQUEST_SIZE) {
            throw new ApiException(ErrorStatus.MAX_UPLOAD_REQUEST_SIZE_EXCEEDED);
        }

        long distinctFileNames = files.stream().map(MultipartFile::getOriginalFilename).distinct().count();

        if (distinctFileNames != files.size()) {
            throw new ApiException(ErrorStatus.NOT_UNIQUE_FILENAME);
        }

        this.files = files;
    }
}
