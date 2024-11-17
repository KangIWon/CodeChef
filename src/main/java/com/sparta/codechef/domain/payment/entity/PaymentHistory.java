package com.sparta.codechef.domain.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String orderId;
    @Column
    private int amount;
    @Column
    private String orderName;
    @Column
    private LocalDateTime approveAt;
    @Column
    private String status;
    @Column
    private String code;
    @Column
    private String message;
    @Column
    private String lastTransactionKey;
    @Column
    private String paymentKey;
    @Column
    private String currency;
    @Column (unique = true)
    private String paymentIdempotencyKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billing_key_id")
    private BillingKey billingKey;

    /**
     * Toss 결제 승인 응답으로 상태 및 필드 업데이트
     * @param response Toss Payments의 결제 승인 응답
     */
    public void updateFromTossApprovalResponse(Map<String, Object> response) {
        this.orderId = (String) response.get("orderId");
        this.amount = ((Number) response.get("totalAmount")).intValue();
        this.orderName = (String) response.get("orderName");
        String approvedAtStr = (String) response.get("approvedAt");
        // approvedAt 필드 파싱 (ISO_OFFSET_DATE_TIME 형식)
        if (approvedAtStr != null && !approvedAtStr.isEmpty()) {
            OffsetDateTime offsetDateTime = OffsetDateTime.parse(approvedAtStr,
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            this.approveAt = offsetDateTime.toLocalDateTime();
        }
        this.status = (String) response.get("status");
        this.paymentKey = (String) response.get("paymentKey");
        this.lastTransactionKey = (String) response.get("lastTransactionKey");
        this.currency = (String) response.get("currency");
    }

    /**
     * 결제 실패 시 오류 정보를 업데이트하는 메서드
     *
     * @param code    에러 코드
     * @param message 에러 메시지
     */
    public void updateFailureInfo(String code, String message) {
        this.status = "FAILED";
        this.code = code;
        this.message = message;
    }

    public void updateStatus(String canceled, Object o, Object o1) {
        this.status = "CANCELED";
    }

    public void updateIdempotencyKey(String idempotencyKey) {
        this.paymentIdempotencyKey = idempotencyKey;
    }
}
