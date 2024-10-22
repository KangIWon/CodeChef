package com.sparta.codechef.domain.point.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PointQueryDslRepositoryImpl implements PointQueryDslRepository {

    private final JPAQueryFactory jpaQueryFactory;
}
