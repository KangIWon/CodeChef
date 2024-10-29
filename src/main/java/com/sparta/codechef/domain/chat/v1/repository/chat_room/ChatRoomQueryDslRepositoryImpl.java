package com.sparta.codechef.domain.chat.v1.repository.chat_room;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.codechef.domain.chat.v1.dto.response.ChatRoomGetResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.querydsl.core.types.ExpressionUtils.count;
import static com.sparta.codechef.domain.chat.v1.entity.QChatRoom.chatRoom;
import static com.sparta.codechef.domain.user.entity.QUser.user;

@Repository
@RequiredArgsConstructor
public class ChatRoomQueryDslRepositoryImpl implements ChatRoomQueryDslRepository {

    private final JPAQueryFactory jpaQueryFactory;


    @Override
    public Page<ChatRoomGetResponse> findAllChatRoom(Pageable pageable) {

        Long total = jpaQueryFactory
                .select(count(chatRoom.id))
                .from(chatRoom)
                .where(chatRoom.isDeleted.isFalse())
                .fetchFirst();

        if (total == null) {
            total = 0L;
        }

        List<ChatRoomGetResponse> results = jpaQueryFactory
                .select(
                        Projections.constructor(
                                ChatRoomGetResponse.class,
                                chatRoom.id,
                                chatRoom.title,
                                chatRoom.password.isNotNull(),
                                user.id.countDistinct(),
                                chatRoom.maxParticipants
                        )
                )
                .from(chatRoom)
                .join(user)
                .on(user.chatRoom.id.eq(chatRoom.id))
                .where(chatRoom.isDeleted.isFalse())
                .groupBy(chatRoom.id)
                .orderBy(chatRoom.id.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(results, pageable, total);
    }


    @Override
    public boolean existsByIdAndUserId(Long chatRoomId, Long userId) {
        Long countChatRoom = jpaQueryFactory
                .select(
                        chatRoom.count()
                )
                .from(chatRoom)
                .where(
                        chatRoom.id.eq(chatRoomId)
                                .and(chatRoom.user.id.eq(userId))
                                .and(chatRoom.isDeleted.isFalse())
                )
                .fetchFirst();

        if (countChatRoom == null) {
            return false;
        }

        return countChatRoom == 1L;
    }
}
