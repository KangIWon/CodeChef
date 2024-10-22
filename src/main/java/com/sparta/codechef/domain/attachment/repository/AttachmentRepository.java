package com.sparta.codechef.domain.attachment.repository;

import com.sparta.codechef.domain.attachment.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long>, AttachmentQueryDslRepository {
}
