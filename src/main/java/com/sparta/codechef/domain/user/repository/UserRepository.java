package com.sparta.codechef.domain.user.repository;

import com.sparta.codechef.domain.user.entity.User;
import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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
    List<User> decreaseAutomatically(LocalDate date,LocalDate today);


    @Query("SELECT u FROM User u WHERE u.id = :userId AND u.chatRoom.id = :chatRoomId")
    Optional<User> findChatRoomUser(Long userId, Long chatRoomId);

    @Modifying
    @Query("UPDATE User u SET u.point = 0")
    void resetUserPoint();

    @Modifying
    @Query("UPDATE User u SET u.point = u.point + :point WHERE u.id = :id")
    void updatePoints(@Param("point") int point, @Param("id") long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdWithPessimisticLock(@Param("id") Long id);
}
