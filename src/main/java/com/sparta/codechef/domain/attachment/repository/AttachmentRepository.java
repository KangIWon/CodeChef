package com.sparta.codechef.domain.attachment.repository;

import com.sparta.codechef.domain.attachment.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long>, AttachmentQueryDslRepository {

    @Query("SELECT a FROM Attachment a WHERE a.board.id = :boardId ORDER BY a.id")
    List<Attachment> findAllByBoardId(Long boardId);
}
