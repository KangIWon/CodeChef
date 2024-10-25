package com.sparta.codechef.domain.user.repository;

import com.sparta.codechef.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, UserQueryDslRepository {
    @Query("SELECT u FROM User u WHERE u.email=:email")
    Optional<User> findByEmail(@Param("email") String email);

    boolean existsByEmail(String email);

    @Modifying
    @Query("UPDATE User u SET u.isAttended = false")
    void resetIsAttend();

    @Query("SELECT count(u) FROM User u WHERE u.chatRoom.id = :chatRoomId")
    int countAllByChatRoom(Long chatRoomId);

    @Query("SELECT exists(SELECT u FROM User u WHERE u.id = :userId AND u.chatRoom.id = :chatRoomId)")
    boolean existsUserByIdAndChatRoomId(Long userId, Long chatRoomId);

    @Query("SELECT u FROM User u WHERE u.id = :userId AND u.chatRoom.id = :chatRoomId")
    Optional<User> findChatRoomUser(Long userId, Long chatRoomId);
}
