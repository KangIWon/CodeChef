package com.sparta.codechef.domain.user.repository;

import com.sparta.codechef.domain.user.entity.User;

import java.util.Optional;

public interface UserQueryDslRepository {

    Optional<User> findNextHost(Long userId, Long chatRoomId);
}
