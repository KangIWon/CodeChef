package com.sparta.codechef.domain.comment.service;

import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.exception.ApiException;
import com.sparta.codechef.domain.board.entity.Board;
import com.sparta.codechef.domain.board.repository.BoardRepository;
import com.sparta.codechef.domain.comment.dto.CommentRequest;
import com.sparta.codechef.domain.comment.dto.CommentResponse;
import com.sparta.codechef.domain.comment.dto.CommentUpdateResponse;
import com.sparta.codechef.domain.comment.entity.Comment;
import com.sparta.codechef.domain.comment.exception.CommentListNotFoundException;
import com.sparta.codechef.domain.comment.exception.CommentNotFoundException;
import com.sparta.codechef.domain.comment.repository.CommentRepository;
import com.sparta.codechef.domain.user.entity.User;
import com.sparta.codechef.domain.user.repository.UserRepository;
import com.sparta.codechef.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Transactional
    public Void createComment(AuthUser authUser, Long boardId, CommentRequest commentRequest) {
        User user = userRepository.findById(authUser.getUserId()).orElseThrow(()->new ApiException(ErrorStatus.NOT_FOUND_USER));
        Board board = boardRepository.findById(boardId).orElseThrow(()->new ApiException(ErrorStatus.NOT_FOUND_BOARD));

        Comment comment = Comment.builder().content(commentRequest.getComment()).user(user).board(board).build();
        commentRepository.save(comment);

        // Redis로 알림 메시지 발행 (userId 포함)
        String channel = "commentNotifications";
        String message = "게시판 ID: " + boardId + "에 새로운 댓글이 작성되었습니다: " + comment.getContent() + " (작성자 ID: " + user.getId() + ")";
        redisTemplate.convertAndSend(channel, message);

        return null;
    }

    public List<CommentResponse> getComments(AuthUser authUser) {
        List<Comment> commentList = commentRepository.findAllByUserId(authUser.getUserId()).orElseThrow(()
            -> new CommentListNotFoundException(ErrorStatus.NOT_FOUND_COMMENT_LIST));
        return commentList.stream().map(comment -> new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getUser().getId(),
                comment.getBoard().getId(),
                comment.getIsAdopted())).toList();
    }

    @Transactional
    public CommentUpdateResponse updateComment(AuthUser authUser, Long boardId, Long commentId, CommentRequest commentRequest) {
        Board board = boardRepository.findById(boardId).orElseThrow(()->new ApiException(ErrorStatus.NOT_FOUND_BOARD));
        System.out.println("board = " + board);

        Comment comment = commentRepository.findByCommentIdAndUserIdAndBoardId(commentId,authUser.getUserId(),board.getId()).orElseThrow(()
                -> new CommentNotFoundException(ErrorStatus.NOT_FOUND_COMMENT));

        System.out.println("comment = " + comment);
        comment.update(commentRequest.getComment());
        commentRepository.save(comment);
        return new CommentUpdateResponse(comment.getContent());
    }

    @Transactional
    public Void deleteComment(AuthUser authUser, Long boardId, Long commentId){
        Board board = boardRepository.findById(boardId).orElse(null);
        Comment comment = commentRepository.findByCommentIdAndUserIdAndBoardId(commentId,authUser.getUserId(),board.getId()).orElseThrow(()
                -> new CommentNotFoundException(ErrorStatus.NOT_FOUND_COMMENT));
        if(authUser.getUserId().equals(comment.getUser().getId()))
        {
            commentRepository.delete(comment);
            return null;
        }
        else throw new ApiException(ErrorStatus.FORBIDDEN_TOKEN);
    }


    @Transactional
    public Void adoptedComment(AuthUser authUser, Long boardId, Long commentId){
        boardRepository.findById(boardId).orElseThrow(
                () -> new ApiException(ErrorStatus.NOT_FOUND_BOARD)
        );

        List<Comment> commentList = commentRepository.findCommentByBoardId(boardId).orElseThrow(
                () -> new ApiException(ErrorStatus.NOT_FOUND_COMMENT)
        );

        if (commentList.stream()
                .anyMatch(Comment::getIsAdopted)) {
            throw new ApiException(ErrorStatus.ALREADY_ADOPTED_COMMENT);
        }
      
        Comment comment = commentRepository.findById(commentId).orElseThrow(()
                -> new CommentNotFoundException(ErrorStatus.NOT_FOUND_COMMENT));
        comment.getUser().addPointToCommentUser();

        comment.isAdopted();
        commentRepository.save(comment);
        pointsAreSavedInRedis(comment.getUser());

        Long id = comment.getUser().getId();
        Integer point = comment.getUser().getPoint();
        String redisKey = comment.getUser().checkRedisKey(comment.getUser());
        redisTemplate.opsForZSet().add(redisKey, id, point);
      
        return null;
    }

    private void pointsAreSavedInRedis(User save) {
        // Redis에 이메일을 키로, 포인트를 값으로 저장
        String redisKey = save.getEmail(); // 유저 이메일을 키로 사용
        Integer points = save.getPoint(); // 유저의 현재 포인트 값

        LocalDateTime now = LocalDateTime.now();
        // 다음 달 첫 번째 날 자정 시간
        LocalDateTime nextMonthStart = now.with(TemporalAdjusters.firstDayOfNextMonth())
                .withHour(0).withMinute(0)
                .withSecond(0).withNano(0);
        Duration ttl = Duration.ofMillis(Duration.between(now, nextMonthStart).toMillis());

//        redisTemplate.opsForValue().set(redisKey, points, ttl); // Redis에 저장
        redisTemplate.opsForZSet().add("userPoints", redisKey, points);
        redisTemplate.expire("userPoints", ttl); // ZSET 전체 TTL을 6일로 설정
    }
}
