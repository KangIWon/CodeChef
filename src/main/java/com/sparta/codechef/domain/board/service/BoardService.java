package com.sparta.codechef.domain.board.service;

import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.enums.UserRole;
import com.sparta.codechef.common.exception.ApiException;
import com.sparta.codechef.domain.board.dto.request.BoardCreatedRequest;
import com.sparta.codechef.domain.board.dto.request.BoardModifiedRequest;
import com.sparta.codechef.domain.board.dto.response.BoardDetailResponse;
import com.sparta.codechef.domain.board.dto.response.BoardResponse;
import com.sparta.codechef.domain.board.entity.Board;
import com.sparta.codechef.domain.board.repository.BoardRepository;
import com.sparta.codechef.domain.comment.dto.CommentResponse;
import com.sparta.codechef.domain.user.entity.User;
import com.sparta.codechef.domain.user.repository.UserRepository;
import com.sparta.codechef.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    /**
     * 게시물 작성
     * @param authUser : 로그인 유저 정보
     * @param request : 게시판 생성에 필요한 request
     * */
    @Transactional
    public Void createBoard(BoardCreatedRequest request, AuthUser authUser) {

        User savedUsers = userRepository.findById(authUser.getUserId()).orElseThrow( // userId는 나중에 authUser.getUserId로 변경
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

    /**
     *  전체 게시물 보기
     * @param page : 10번 페이지 중 현재 페이지 번호
     * @param size : 한 페이지에 보여줄 게시물 수
     * */
    public Page<BoardResponse> findAllBoard(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "id"));
        // Board 엔티티를 BoardResponse로 변환
        return boardRepository.findAll(pageable)
                .map(board -> new BoardResponse(board.getId(), board.getUser().getId(),
                        board.getTitle(),
                        board.getContents(),
                        board.getLanguage().toString(),
                        board.getFramework()));  // 결과를 List로 반환
    }


    /**
     * 게시물 단건 조회
     * @param boardId : 보려고 하는 게시물 번호
     * */
    public BoardDetailResponse getBoard(Long boardId) {

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


    /**
     * 게시물 수정
     * @param authUser : 로그인 유저 정보
     * @param request : 게시판 수정에 필요한 request
     * @param boardId : 수정 하려는 게시물 번호
     * */
    @Transactional
    public Void modifiedBoard(Long boardId, BoardModifiedRequest request, AuthUser authUser) {

        Board board = boardRepository.findById(boardId).orElseThrow(
                () -> new ApiException(ErrorStatus.NOT_FOUND_BOARD)
        );
        if (!board.getUser().getId().equals(authUser.getUserId()) && !authUser.getAuthorities().equals(UserRole.ROLE_ADMIN))
            throw new ApiException(ErrorStatus.NOT_THE_AUTHOR);

        board.BoardModify(
                request.getTitle(),
                request.getContents(),
                request.getLanguage(),
                request.getFramework()
        );

        return null;
    }


    /**
     * 게시물 삭제
     * @param authUser : 로그인 유저 정보
     * @param boardId : 수정 하려는 게시물 번호
     **/
    @Transactional
    public Void deletedBoard(Long boardId, AuthUser authUser) {
        Board board = boardRepository.findById(boardId).orElseThrow(
                () -> new ApiException(ErrorStatus.NOT_FOUND_BOARD)
        );

        if (!board.getUser().getId().equals(authUser.getUserId()) && !authUser.getAuthorities().equals(UserRole.ROLE_ADMIN))
            throw new ApiException(ErrorStatus.NOT_THE_AUTHOR);

        boardRepository.deleteById(boardId);

        return null;
    }

    /**
     * 게시물 검색
     * @param  title : 제목으로 검색
     * @param  content : 내용으로 검색
     * @param  page : 10번 페이지 중 현재 페이지 번호
     * @param  size : 한 페이지에 보여줄 게시물 수
     **/
    public Page<BoardResponse> boardSearch(String title, String content, int page, int size) {
        Pageable pageable = PageRequest.of(page-1, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Board> boards = boardRepository.boardSearch(title, content, pageable);


        return boards.map(board -> new BoardResponse(
                board.getId(),
                board.getUser().getId(),
                board.getTitle(),
                board.getContents(),
                board.getLanguage().toString(),
                board.getFramework()));
    }

    /**
     * 자기가 쓴 게시물 보기
     * @param authUser : 로그인 유저 정보
     * @param page : 10번 페이지 중 현재 페이지 번호
     * @param size : 한 페이지에 보여줄 게시물 수
     * */
    public Page<BoardResponse> myCreatedBoard(AuthUser authUser,int page, int size) {

        Pageable pageable = PageRequest.of(page-1, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Board> allById = boardRepository.findAllByUserId(authUser.getUserId(), pageable).orElseThrow(
                () -> new ApiException(ErrorStatus.NOT_FOUND_USER)
        );


        return allById.map(board -> new BoardResponse(
                board.getId(),
                board.getUser().getId(),
                board.getTitle(),
                board.getContents(),
                board.getLanguage().toString(),
                board.getFramework()));
    }
}
