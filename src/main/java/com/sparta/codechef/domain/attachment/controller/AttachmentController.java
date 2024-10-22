package com.sparta.codechef.domain.attachment.controller;

import com.sparta.codechef.domain.attachment.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AttachmentController {
    private final AttachmentService attachmentService;
}
