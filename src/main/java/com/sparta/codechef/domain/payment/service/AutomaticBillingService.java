package com.sparta.codechef.domain.payment.service;

import com.sparta.codechef.domain.payment.entity.BillingKey;
import com.sparta.codechef.domain.payment.repository.BillingKeyRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AutomaticBillingService {

    private final BillingKeyRepository billingKeyRepository;
    private final PaymentService paymentService;

    @Scheduled(cron = "0 0 0 * * *")
    public void performAutomaticBilling() {
        log.info("자동 결제 작업 시작: {}", LocalDateTime.now());

        // 빌링키들 활성화 상태 && 빌링데이트 오늘인 것들.
        List<BillingKey> dueBillingKeys = billingKeyRepository.findByStatusAndBillingDateEquals(
                "ACTIVE", LocalDate.now());

        log.info("자동 결제 대상 BillingKey 개수: {}", dueBillingKeys.size());

        for (BillingKey dueBillingKey : dueBillingKeys) {
            Long id = dueBillingKey.getUser().getId();
            try {
                paymentService.processPayment(id,10000);
                log.info("사용자 [{}]의 자동 결제가 성공적으로 처리되었습니다.", dueBillingKey.getUser().getId());
            } catch (Exception e) {
                log.error("사용자 [{}]의 자동 결제 처리 중 오류 발생: {}", dueBillingKey.getUser().getId(), e.getMessage());
            }
        }
        log.info("자동 결제 작업 완료: {}", LocalDateTime.now());
    }
}
