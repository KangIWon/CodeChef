package com.sparta.codechef.domain.event.controller;

import com.sparta.codechef.common.ApiResponse;
import com.sparta.codechef.domain.event.service.EventService;
import com.sparta.codechef.security.AuthUser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/event")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping("/start")
    public ApiResponse eventStart(@AuthenticationPrincipal AuthUser authUser){
        return ApiResponse.ok("이벤트가 시작 되었습니다.", eventService.eventStart(authUser));
    }

    @PostMapping
    public ApiResponse event(@AuthenticationPrincipal AuthUser authUser) {
        return ApiResponse.ok("", eventService.eventPoints(authUser));

    }
}
