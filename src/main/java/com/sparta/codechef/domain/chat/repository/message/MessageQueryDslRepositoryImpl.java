package com.sparta.codechef.domain.chat.repository.message;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MessageQueryDslRepositoryImpl implements MessageQueryDslRepository {

    private final JPAQueryFactory jpaQueryFactory;
}
