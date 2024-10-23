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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public Void createComment(AuthUser authUser, Long boardId, CommentRequest commentRequest) {
        User user = userRepository.findById(authUser.getUserId()).orElse(null);
        Board board = boardRepository.findById(boardId).orElse(null);

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
        Board board = boardRepository.findById(boardId).orElse(null);
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
//            throw new ApiException(ErrorStatus.EXAMPLE_ERROR);
        }
        return null;
    }


    @Transactional
    public Void adoptedComment(AuthUser authUser, Long boardId, Long commentId){
        Board board = boardRepository.findById(boardId).orElse(null);
        Comment comment = commentRepository.findByCommentIdAndUserIdAndBoardId(commentId,authUser.getUserId(),board.getId()).orElseThrow(()
                -> new CommentNotFoundException(ErrorStatus.NOT_FOUND_COMMENT));
        comment.isAdopted(comment.getIsAdopted());
        commentRepository.save(comment);

        return null;
    }
}
