package com.sparta.codechef.domain.chat.repository.message;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.codechef.domain.chat.dto.response.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.sparta.codechef.domain.chat.entity.QMessage.message1;


@Repository
@RequiredArgsConstructor
public class MessageQueryDslRepositoryImpl implements MessageQueryDslRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<MessageResponse> findAllByChatRoomId(Long chatRoomId) {

        return jpaQueryFactory.select(
                Projections.constructor(
                        MessageResponse.class,
                        message1.id,
                        message1.message,
                        message1.user.id,
                        message1.user.email,
                        message1.createdAt

                ))
                .from(message1)
                .where(message1.chatRoom.id.eq(chatRoomId))
                .fetch();
    }
}
