package com.sparta.codechef.domain.chatRoom.repository.chat_room;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChatRoomQueryDslRepositoryImpl implements ChatRoomQueryDslRepository {
    private final JPAQueryFactory jpaQueryFactory;
}
