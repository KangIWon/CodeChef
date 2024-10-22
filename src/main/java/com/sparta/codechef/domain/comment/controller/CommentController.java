package com.sparta.codechef.domain.comment.controller;

import com.sparta.codechef.domain.comment.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
}
