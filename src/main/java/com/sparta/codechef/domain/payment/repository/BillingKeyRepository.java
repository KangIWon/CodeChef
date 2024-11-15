package com.sparta.codechef.domain.payment.repository;

import com.sparta.codechef.domain.payment.entity.BillingKey;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingKeyRepository extends JpaRepository<BillingKey, Long> {
      Optional<BillingKey> findByUserId(Long id);
      boolean existsPersonalBillingKeyByUserId(Long userId);

      List<BillingKey> findByStatusAndBillingDateEquals(String status, LocalDate now);
}