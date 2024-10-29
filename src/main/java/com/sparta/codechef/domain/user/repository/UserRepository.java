package com.sparta.codechef.domain.user.repository;

import com.sparta.codechef.domain.user.entity.User;
import java.time.LocalDate;
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
    @Query("UPDATE User u SET u.isAttended = false WHERE u.isAttended = true ")
    void resetIsAttend();

    @Query("SELECT count(u) FROM User u WHERE u.chatRoom.id = :chatRoomId")
    int countAllByChatRoom(Long chatRoomId);

    @Modifying
    @Query("UPDATE User u SET u.point = GREATEST(CAST(u.point * 0.9 AS integer), 0), u.lastAttendDate = :today WHERE u.lastAttendDate < :date")
    void decreaseAutomatically(LocalDate date,LocalDate today);


    @Query("SELECT u FROM User u WHERE u.id = :userId AND u.chatRoom.id = :chatRoomId")
    Optional<User> findChatRoomUser(Long userId, Long chatRoomId);
}
