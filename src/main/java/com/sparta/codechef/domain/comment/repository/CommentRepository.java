package com.sparta.codechef.domain.comment.repository;

import com.sparta.codechef.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long>, CommentQueryDslRepository {

    Optional<Comment> findByUserIdAndBoardId(Long userId, Long boardId);
    Optional<Comment> findByCommentIdAndUserIdAndBoardId(Long commentId,Long userId, Long boardId);

    Optional<List<Comment>> findAllByUserId(Long userId);
}
