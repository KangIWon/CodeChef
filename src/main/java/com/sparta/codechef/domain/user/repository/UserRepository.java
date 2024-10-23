package com.sparta.codechef.domain.user.repository;

import com.sparta.codechef.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, UserQueryDslRepository {

    @Query("SELECT count(u) FROM User u WHERE u.chatRoom.id = :chatRoomId")
    int countAllByChatRoom(Long chatRoomId);

    @Query("SELECT exists(SELECT u FROM User u WHERE u.id = :userId AND u.chatRoom.id = :chatRoomId)")
    boolean existsUserByIdAndChatRoomId(Long userId, Long chatRoomId);
}
