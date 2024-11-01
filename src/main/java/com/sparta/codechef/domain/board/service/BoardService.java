package com.sparta.codechef.domain.board.service;

import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.exception.ApiException;
import com.sparta.codechef.domain.board.dto.request.BoardCreatedRequest;
import com.sparta.codechef.domain.board.dto.request.BoardDetailEvent;
import com.sparta.codechef.domain.board.dto.request.BoardModifiedRequest;
import com.sparta.codechef.domain.board.dto.response.BoardDetailResponse;
import com.sparta.codechef.domain.board.dto.response.BoardResponse;
import com.sparta.codechef.domain.board.entity.Board;
import com.sparta.codechef.domain.board.repository.BoardRepository;
import com.sparta.codechef.domain.comment.dto.CommentResponse;
import com.sparta.codechef.domain.comment.entity.Comment;
import com.sparta.codechef.domain.comment.repository.CommentRepository;
import com.sparta.codechef.domain.user.entity.User;
import com.sparta.codechef.domain.user.repository.UserRepository;
import com.sparta.codechef.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CommentRepository commentRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

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

    /**
     * 보드 조회수 증가 및 조회
     * @param authUser : 로그인 유저 정보
     * @param boardId : 조회 하려는 게시물 번호
     * */
    // 보드 조회수 증가 및 조회
    // Board 조회 및 데이터 반환
    // Board 조회 및 조회수 증가
// Board 조회 및 조회수 증가
    @Transactional
    public BoardDetailResponse getBoardDetails(AuthUser authUser, Long boardId) {
        Board board = boardRepository.findByIdWithPessimisticLock(boardId)
                .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND_BOARD));

        List<Comment> commentList = commentRepository.findByBoardId(boardId)
                .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND_COMMENT));

        // 데이터베이스에서 조회수 증가 처리
        incrementDatabaseViewCount(board);

        // 트랜잭션이 성공적으로 커밋된 후에만 Redis에 조회수 반영
        publishViewCountEvent(authUser, boardId);

        // 조회수 증가가 반영된 보드 정보를 반환
        return new BoardDetailResponse(
                board.getId(),
                board.getUser().getId(),
                board.getTitle(),
                board.getContents(),
                board.getLanguage().name(),
                board.getFramework(),
                board.getViewCount(),
                commentList.stream().map(comment -> new CommentResponse(
                        comment.getId(),
                        comment.getContent(),
                        comment.getUser().getId(),
                        comment.getBoard().getId(),
                        comment.getIsAdopted())).toList()
        );
    }

    // 데이터베이스의 조회수 증가
    public void incrementDatabaseViewCount(Board board) {
        board.setViewCount(); // 조회수 증가
        boardRepository.save(board); // 업데이트 반영
    }

    // 트랜잭션 커밋 후 이벤트 발행
    public void publishViewCountEvent(AuthUser authUser, Long boardId) {
        applicationEventPublisher.publishEvent(new BoardDetailEvent(authUser, boardId));
    }

    // 트랜잭션 커밋 후에 Redis 조회수 증가 이벤트 처리
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleViewCountIncrement(BoardDetailEvent event) {
        String redisViewKey = "board:viewcount:" + event.getBoardId();
//        // 아래 코드 어뷰징 확인하는 코드임
//        incrementViewCount(redisViewKey, event.getAuthUser().getUserId().toString());
        Long viewCount = redisTemplate.opsForValue().increment(redisViewKey, 1);//0); 어뷰징 방지를 위해 코드를 수정해야함
        updateRanking(event.getBoardId(), viewCount);
    }



//    Long viewCount = redisTemplate.opsForValue().increment(redisViewKey, 1);//0); 어뷰징 방지를 위해 코드를 수정해야함
//
//    // 아래 코드 어뷰징 확인하는 코드임
//    // incrementViewCount(redisViewKey, authUser.getUserId().toString());

    // 랭킹 업데이트
    private void updateRanking(Long boardId, Long viewCount) {
        String rankingKey = "board:ranking";
        redisTemplate.opsForZSet().add(rankingKey, boardId, viewCount);
    }

    private void incrementViewCount(String redisKey, String userId) {
        String userKey = redisKey + ":user:" + userId;

        if (Boolean.TRUE.equals(redisTemplate.hasKey(userKey))) {
            return; // 이미 조회한 사용자
        }

        redisTemplate.opsForValue().increment(redisKey);
        redisTemplate.opsForValue().set(userKey, "1", Duration.ofHours(24));
    }

    // 인기 보드 랭킹 관리
    @Cacheable(value = "topBoardsCache", key = "'topBoards'")
    public List<BoardResponse> getTopBoards() {
        String rankingKey = "board:ranking";
        // Redis에서 인기 보드 ID 리스트 가져오기 (조회수 기준 내림차순)
        Set<ZSetOperations.TypedTuple<Object>> topBoardIdsWithScores = redisTemplate.opsForZSet()
                .reverseRangeWithScores(rankingKey, 0, 2);
        if (topBoardIdsWithScores == null || topBoardIdsWithScores.isEmpty()) {
            return Collections.emptyList(); // 랭킹에 데이터가 없는 경우 빈 리스트 반환
        }
        // Redis에서 보드 데이터를 가져오고 반환
        List<BoardResponse> topBoards = topBoardIdsWithScores.stream()
                .map(tuple -> {
                    Long boardId = Long.valueOf(tuple.getValue().toString());
                    // Redis에서 보드 상세 정보를 가져옵니다.
                    String redisBoardKey = "board:details:" + boardId;
                    BoardResponse boardResponse = (BoardResponse) redisTemplate.opsForValue().get(redisBoardKey);
                    if (boardResponse == null) {
                        // Redis에 캐시된 정보가 없으면 DB에서 조회 후 Redis에 캐싱
                        Board board = boardRepository.findById(boardId).orElseThrow(
                                () -> new ApiException(ErrorStatus.NOT_FOUND_BOARD)
                        );
                        // BoardResponse 객체 생성 후 Redis에 캐싱
                        boardResponse = new BoardResponse(
                                board.getId(),
                                board.getUser().getId(),
                                board.getTitle(),
                                board.getContents(),
                                board.getLanguage().name(),
                                board.getFramework()
                        );
                        redisTemplate.opsForValue().set(redisBoardKey, boardResponse);
                    }
                    return boardResponse;
                })
                .collect(Collectors.toList());
        return topBoards;
    }
    // 매 시간마다 캐시 리셋
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void resetDailyViewCount() {
        String pattern = "board:viewcount:*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        // 랭킹 캐시 리셋
        redisTemplate.delete("board:ranking");
        // 인기 보드 캐시 리셋
        redisTemplate.delete("topBoardsCache::topBoards");
        // 보드 상세 정보 캐시 리셋
        Set<String> boardDetailsKeys = redisTemplate.keys("board:details:*");
        if (boardDetailsKeys != null && !boardDetailsKeys.isEmpty()) {
            redisTemplate.delete(boardDetailsKeys);
        }
    }
}
