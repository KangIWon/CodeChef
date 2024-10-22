package com.sparta.codechef.domain.user.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserQueryDslRepositoryImpl implements UserQueryDslRepository {

    private final JPAQueryFactory jpaQueryFactory;
}
