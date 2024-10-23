package com.sparta.codechef.domain.board.repository;

import com.sparta.codechef.domain.board.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long>, BoardQueryDslRepository {

    @Query("SELECT b FROM Board b WHERE b.id = :boardId AND b.user.id = :userId")
    Optional<Board> findByIdAndUserId(Long boardId, Long userId);
}
