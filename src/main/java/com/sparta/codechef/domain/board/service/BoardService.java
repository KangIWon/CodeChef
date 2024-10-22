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
    private final CommentRepository commentRepository;

    @Transactional
    public void createBoard(BoardCreatedRequest request, AuthUser authUser) {

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
    }

    public List<BoardResponse> findAllBoard() {
        // Board 엔티티를 BoardResponse로 변환
        return boardRepository.findAll().stream()
                .map(board -> BoardResponse.builder()
                        .id(board.getId())
                        .title(board.getTitle())
                        .content(board.getContents())
                        .language(board.getLanguage().toString())
                        .framework(board.getFramework())
                        .userId(board.getUser().getId())  // userId 빌더 메서드를 사용
                        .build()
                )
                .collect(Collectors.toList());  // 결과를 List로 반환
    }

    public BoardDetailResponse getBoard(Long boardId, AuthUser authUser) {

        Board savedBoard = boardRepository.findById(boardId).orElseThrow(
                () -> new ApiException(ErrorStatus.NOT_FOUND_BOARD)
        );

//        List<CommentResponse> commentResponseDtoList = savedBoard.getComments().stream()
//                .map(comment -> CommentResponse.builder()
//                        .id(comment.getId())
//                        .content(comment.getContent())
//                        .createdAt(comment.getCreatedAt())
//                        .modifiedAt(comment.getModifiedAt())
//                        .userId(comment.getUser().getId())
//                        .build())
//                .collect(Collectors.toList());  // 결과를 List로 변환

        return BoardDetailResponse.builder()
                .id(savedBoard.getId())
                .title(savedBoard.getTitle())
                .content(savedBoard.getContents())
                .language(savedBoard.getLanguage().toString())
                .framework(savedBoard.getFramework())
                .userId(savedBoard.getUser().getId())
                .commentResponseDtoList(
                        savedBoard.getComments().stream()
                        .map(comment -> CommentResponse.builder()
                                .id(comment.getId())
                                .content(comment.getContent())
                                .createdAt(comment.getCreatedAt())
                                .modifiedAt(comment.getModifiedAt())
                                .userId(comment.getUser().getId())
                                .build())
                        .collect(Collectors.toList()))
                .build();

    }

    @Transactional
    public void modifiedBoard(Long boardId, BoardModifiedRequest request, AuthUser authUser) {

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
    }

    @Transactional
    public void deletedBoard(Long boardId, AuthUser authUser, Long userId) {
        Board board = boardRepository.findById(boardId).orElseThrow(
                () -> new ApiException(ErrorStatus.NOT_FOUND_BOARD)
        );

        if (board.equals(userId)) // authUser.getUserId 로 변경 해야 됨
            throw new ApiException(ErrorStatus.NOT_THE_AUTHOR);

        boardRepository.deleteById(boardId);
    }
}
