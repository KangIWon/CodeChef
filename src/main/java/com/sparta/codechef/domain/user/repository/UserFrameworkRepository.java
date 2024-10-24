package com.sparta.codechef.domain.user.repository;

import com.sparta.codechef.domain.user.entity.UserFramework;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserFrameworkRepository extends JpaRepository<UserFramework, Long> {

    Optional<List<UserFramework>> findAllByUserId(Long userId);
    Optional<UserFramework> findByUserIdAndFrameworkId(Long userId, Long frameworkId);
}
