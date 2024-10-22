package com.sparta.codechef.domain.point.service;

import com.sparta.codechef.domain.point.entity.Point;
import com.sparta.codechef.domain.point.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;
    
}
