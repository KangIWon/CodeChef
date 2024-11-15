package com.sparta.codechef.domain.payment.repository;

import com.sparta.codechef.domain.payment.entity.BillingKey;
import com.sparta.codechef.domain.payment.entity.PaymentHistory;
import io.lettuce.core.dynamic.annotation.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {


    Optional<PaymentHistory> findLatestById(Long userId);

    @Query("SELECT ph FROM PaymentHistory ph WHERE ph.billingKey = :billingKey ORDER BY ph.approveAt DESC")
    Optional<PaymentHistory> findLatestByBillingKey(@Param("billingKey") BillingKey billingKey);
}
