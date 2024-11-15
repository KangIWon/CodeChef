package com.sparta.codechef.domain.payment.repository;

import com.sparta.codechef.domain.payment.entity.IdempotencyKey;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, Long> {

}
