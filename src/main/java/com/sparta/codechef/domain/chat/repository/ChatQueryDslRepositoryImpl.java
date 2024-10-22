package com.sparta.codechef.domain.chat.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChatQueryDslRepositoryImpl implements ChatQueryDslRepository {
    private final JPAQueryFactory jpaQueryFactory;
}
