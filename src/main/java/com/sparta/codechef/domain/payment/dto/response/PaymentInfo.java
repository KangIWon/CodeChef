package com.sparta.codechef.domain.payment.dto.response;

import lombok.Getter;

@Getter
public class PaymentInfo {

    private final String customerKey;
    private final String customerEmail;
    private final String customerName;

    public PaymentInfo(String customerKey, String customerEmail, String customerName) {
        this.customerKey = customerKey;
        this.customerEmail = customerEmail;
        this.customerName = customerName;
    }
}
