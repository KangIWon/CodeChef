package com.sparta.codechef.domain.user.repository;

import com.sparta.codechef.domain.user.entity.UserFramework;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFrameworkRepository extends JpaRepository<UserFramework, Long> {
}
