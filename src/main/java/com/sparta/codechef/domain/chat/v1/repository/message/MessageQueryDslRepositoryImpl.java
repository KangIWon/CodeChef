package com.sparta.codechef.domain.chat.v1.repository.message;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.codechef.domain.chat.v1.dto.response.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.sparta.codechef.domain.chat.v1.entity.QMessage.message;

@Repository
@RequiredArgsConstructor
public class MessageQueryDslRepositoryImpl implements MessageQueryDslRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<MessageResponse> findAllByChatRoomId(Long chatRoomId) {
        return jpaQueryFactory.select(
                Projections.constructor(
                        MessageResponse.class,
                        message.id,
                        message.content,
                        message.user.id,
                        message.user.email,
                        message.createdAt

                ))
                .from(message)
                .where(
                        message.chatRoom.id.eq(chatRoomId)
                                .and(message.isDeleted.isFalse())
                )
                .fetch();
    }
}
