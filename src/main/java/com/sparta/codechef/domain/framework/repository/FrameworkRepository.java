package com.sparta.codechef.domain.framework.repository;

import com.sparta.codechef.domain.framework.entity.Framework;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FrameworkRepository extends JpaRepository<Framework,Long> {
}
