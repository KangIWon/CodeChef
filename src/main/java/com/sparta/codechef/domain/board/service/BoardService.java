package com.sparta.codechef.domain.board.service;

import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.exception.ApiException;
import com.sparta.codechef.domain.board.dto.request.BoardCreatedRequest;
import com.sparta.codechef.domain.board.dto.request.BoardModifiedRequest;
import com.sparta.codechef.domain.board.dto.response.BoardDetailResponse;
import com.sparta.codechef.domain.board.dto.response.BoardResponse;
import com.sparta.codechef.domain.board.entity.Board;
import com.sparta.codechef.domain.board.repository.BoardRepository;
import com.sparta.codechef.domain.comment.dto.CommentResponse;
import com.sparta.codechef.domain.comment.repository.CommentRepository;
import com.sparta.codechef.domain.user.entity.User;
import com.sparta.codechef.domain.user.repository.UserRepository;
import com.sparta.codechef.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    @Transactional
    public Void createBoard(BoardCreatedRequest request, AuthUser authUser) {

        User savedUsers = userRepository.findById(request.getUserId()).orElseThrow( // userId는 나중에 authUser.getUserId로 변경
                    () -> new ApiException(ErrorStatus.NOT_FOUND_USER)
                );

        Board board = Board.builder().user(savedUsers)
                .title(request.getTitle())
                .contents(request.getContents())
                .language(request.getLanguage())
                .framework(request.getFramework())
                .build();

        boardRepository.save(board);

        return null;
    }

    public List<BoardResponse> findAllBoard() {
        // Board 엔티티를 BoardResponse로 변환
        return boardRepository.findAll().stream()
                .map(board -> new BoardResponse(board.getId(), board.getUser().getId(),
                        board.getTitle(),
                        board.getContents(),
                        board.getLanguage().toString(),
                        board.getFramework()))
                .collect(Collectors.toList());  // 결과를 List로 반환
    }

    public BoardDetailResponse getBoard(Long boardId, AuthUser authUser) {

        Board savedBoard = boardRepository.findById(boardId).orElseThrow(
                () -> new ApiException(ErrorStatus.NOT_FOUND_BOARD)
        );


        return new BoardDetailResponse(savedBoard.getId(),
                savedBoard.getUser().getId(),
                savedBoard.getTitle(),
                savedBoard.getContents(),
                savedBoard.getLanguage().toString(),
                savedBoard.getFramework(),
                savedBoard.getComments().stream().map(comment -> new CommentResponse(
                        comment.getId(),
                        comment.getContent(),
                        comment.getUser().getId(),
                        comment.getBoard().getId(),
                        comment.getIsAdopted())).toList());

    }

    @Transactional
    public Void modifiedBoard(Long boardId, BoardModifiedRequest request, AuthUser authUser) {

        Board board = boardRepository.findById(boardId).orElseThrow( //
                () -> new ApiException(ErrorStatus.NOT_FOUND_BOARD)
        );

        if (board.equals(request.userId)) // authUser.getUserId 로 변경 해야 됨
            throw new ApiException(ErrorStatus.NOT_THE_AUTHOR);

        board.BoardModify(
                request.getTitle(),
                request.getContents(),
                request.getLanguage(),
                request.getFramework()
        );

        return null;
    }

    @Transactional
    public Void deletedBoard(Long boardId, AuthUser authUser, Long userId) {
        Board board = boardRepository.findById(boardId).orElseThrow(
                () -> new ApiException(ErrorStatus.NOT_FOUND_BOARD)
        );

        if (board.equals(userId)) // authUser.getUserId 로 변경 해야 됨
            throw new ApiException(ErrorStatus.NOT_THE_AUTHOR);

        boardRepository.deleteById(boardId);

        return null;
    }
}
