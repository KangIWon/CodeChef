package com.sparta.codechef.domain.payment.controller;

import com.sparta.codechef.common.ApiResponse;
import com.sparta.codechef.domain.payment.dto.request.CancelSubscriptionRequest;
import com.sparta.codechef.domain.payment.dto.request.PaymentRequest;
import com.sparta.codechef.domain.payment.dto.response.PaymentInfo;
import com.sparta.codechef.domain.payment.service.PaymentService;
import com.sparta.codechef.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PaymentApiController {

    private final PaymentService paymentService;
    private final String clientKey;


    // 인증된 유저의 customerKey 반환 or 생성해서 반환
    @GetMapping("/get-customer-key")
    public ApiResponse<PaymentInfo> getCustomerKey(@AuthenticationPrincipal AuthUser authUser) {
        Long userId = authUser.getUserId();
        PaymentInfo paymentInfo = new PaymentInfo(paymentService.getOrCreateCustomerKey(userId),
                authUser.getEmail(),
                authUser.getUserName());
        return ApiResponse.ok("커스터머키 발급 완료 ",paymentInfo);
    }

    //
    @GetMapping("/client-key")
    public String getClientKey() {
        return clientKey;
    }

    // 카드 등록 성공
    @PostMapping("/success")
    public ApiResponse<Void> handleSuccess(
            @RequestParam("customerKey") String customerKey,
            @RequestParam("authKey") String authKey
    ) {
        paymentService.processBillingKey(customerKey, authKey);
        return ApiResponse.ok("카드 등록을 성공했습니다.", null);
    }

    // 카드 등록 실패
    @PostMapping("/fail")
    public ApiResponse<Void> handleFailure(
            @RequestParam("code") String code,
            @RequestParam("message") String message,
            @RequestParam(value = "orderId", required = false) String orderId) {
        paymentService.handleFailure(code, message);
        return ApiResponse.ok("카드 등록을 실패하셨습니다.", null);
    }

    // 결제 api
    @PostMapping("/payments")
    public ApiResponse<Void> processPayment(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody PaymentRequest paymentRequest) {
        Long userId = authUser.getUserId();

        paymentService.processPayment(userId, paymentRequest.getAmount());
        return ApiResponse.ok("결제가 완료되었습니다.", null);
    }

    @PostMapping("/refund")
    public ApiResponse<Void> cancelBilling(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody CancelSubscriptionRequest cancelSubscriptionRequest
            ) {
        Long userId = authUser.getUserId();
        paymentService.cancelBilling(userId,cancelSubscriptionRequest);
        return ApiResponse.ok("서비스 규정에 맞게 환불이 되었습니다.", null);
    }

}
