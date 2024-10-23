package com.sparta.codechef.domain.attachment.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AttachmentResponse {
    private final Long id;
    private final String name;
    private final String fileUrl;
}
