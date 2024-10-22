package com.sparta.codechef.domain.point.controller;

import com.sparta.codechef.domain.point.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;
}
