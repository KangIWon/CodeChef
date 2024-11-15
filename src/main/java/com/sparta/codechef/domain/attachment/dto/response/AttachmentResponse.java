package com.sparta.codechef.domain.attachment.dto.response;

import com.sparta.codechef.domain.attachment.entity.Attachment;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AttachmentResponse {
    private final Long id;
    private final String name;
    private final String fileUrl;

    public AttachmentResponse(Attachment attachment) {
        this.id = attachment.getId();
        this.name = attachment.getS3Key();
        this.fileUrl = attachment.getCdnUrl();
    }
}
