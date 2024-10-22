package com.sparta.codechef.domain.user.repository;

import com.sparta.codechef.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, UserQueryDslRepository {
}
