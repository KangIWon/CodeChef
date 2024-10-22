package com.sparta.codechef.domain.comment.controller;

import com.sparta.codechef.domain.comment.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/boards/{boardId}/")
public class CommentController {

    private final CommentService commentService;
}
