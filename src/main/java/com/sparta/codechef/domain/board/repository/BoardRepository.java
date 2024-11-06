package com.sparta.codechef.domain.board.repository;

import com.sparta.codechef.domain.board.entity.Board;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long>, BoardQueryDslRepository {

    @Query("SELECT c FROM Board c WHERE c.user.id = :userId")
    Page<Board> findAllByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT c FROM Board c " +
            "WHERE (:title IS NULL OR c.title LIKE %:title%) " +
            "AND (:content IS NULL OR c.contents LIKE %:content%)")
    Page<Board> boardSearch(@Param("title") String title,
                            @Param("content") String content,
                            Pageable pageable);

    @Query("SELECT EXISTS(SELECT b FROM Board b WHERE b.id = :boardId AND b.user.id = :userId)")
    boolean existsByIdAndUserId(Long userId, Long boardId);

    // 조회수를 기준으로 상위 3개의 보드를 내림차순으로 반환
    List<Board> findTop3ByOrderByViewCountDesc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Board b WHERE b.id = :boardId")
    Optional<Board> findByIdWithPessimisticLock(@Param("boardId") Long boardId);
}
