package com.sparta.codechef.domain.attachment.dto.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class AttachmentRequest {
    private final List<MultipartFile> files;
}
