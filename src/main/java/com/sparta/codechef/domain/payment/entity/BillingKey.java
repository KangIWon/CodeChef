package com.sparta.codechef.domain.payment.entity;

import com.sparta.codechef.common.Timestamped;
import com.sparta.codechef.domain.payment.status.BillingKeyStatus;
import com.sparta.codechef.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "billing_keys")
public class BillingKey extends Timestamped {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "billing_key", unique = true, length = 200)
    private String personalBillingKey;

    @Column
    @Builder.Default
    private LocalDate billingDate = null;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    private BillingKeyStatus status;

    /**
     * 예약된 결제 일자 및 상태 업데이트
     */
    public void updateScheduledDate(LocalDateTime approveAt) {
        if (approveAt != null) {
            this.billingDate = approveAt.toLocalDate().plusMonths(1);
        } else {
            this.billingDate = LocalDate.now().plusMonths(1);
        }
    }

    public void updateBillingKey(String userBillingKey) {
        this.personalBillingKey = userBillingKey;
        this.status = BillingKeyStatus.ACTIVE;
    }

    public void cancelSubscription() {
        this.status = BillingKeyStatus.CANCELED;
    }
}
