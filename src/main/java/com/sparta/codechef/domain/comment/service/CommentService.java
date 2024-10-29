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
        User user = userRepository.findById(authUser.getUserId()).orElseThrow(()->new ApiException(ErrorStatus.NOT_FOUND_USER));
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND_BOARD));
        Comment comment = commentRepository.findByIdAndBoardId(commentId,board.getId()).orElseThrow(()
                -> new CommentNotFoundException(ErrorStatus.NOT_FOUND_COMMENT));

        if(comment.getIsAdopted()){
            throw new ApiException(ErrorStatus.ALREADY_ADOPTED_COMMENT);
        }
        comment.isAdopted();
        commentRepository.save(comment);

        comment.getUser().addPointToCommentUser();
        userRepository.save(user);

        Long id = comment.getUser().getId();
        Integer point = comment.getUser().getPoint();
        String redisKey = comment.getUser().checkRedisKey(comment.getUser());
        redisTemplate.opsForZSet().add(redisKey, id, point);
        return null;
    }
}
