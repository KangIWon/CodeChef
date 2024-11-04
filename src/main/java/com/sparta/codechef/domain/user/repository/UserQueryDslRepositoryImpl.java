package com.sparta.codechef.domain.user.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.codechef.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.sparta.codechef.domain.user.entity.QUser.user;

@Repository
@RequiredArgsConstructor
public class UserQueryDslRepositoryImpl implements UserQueryDslRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<User> findNextHost(Long userId, Long chatRoomId) {
        return Optional.ofNullable(
                jpaQueryFactory
                        .select(user)
                        .from(user)
                        .where(
                                user.chatRoom.id.eq(chatRoomId)
                                        .and(user.id.ne(userId))
                        )
                        .orderBy(user.id.asc())
                        .limit(1)
                        .fetchFirst()
        );
    }

    @Override
    public boolean existsUserByIdAndChatRoomId(Long userId, Long chatRoomId) {
        Long countUser = jpaQueryFactory
                .select(
                        user.count()
                )
                .from(user)
                .where(
                        user.id.eq(userId)
                                .and(user.chatRoom.id.eq(chatRoomId))
                )
                .fetchFirst();

        if (countUser == null) {
            return false;
        }

        return countUser == 1L;
    }
}
