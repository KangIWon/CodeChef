package com.sparta.codechef.domain.attachment.service;

import com.sparta.codechef.domain.attachment.repository.AttachmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
}
