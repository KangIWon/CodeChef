package com.sparta.codechef.domain.payment.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class PaymentViewController {

    @GetMapping("/api/user/login-page")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/payment")
    public String paymentPage() {
        return "payment";
    }

    @GetMapping("/success")
    public String successPage() {
        return "success";
    }

    @GetMapping("/fail")
    public String failPage() {
        return "fail";
    }

    @GetMapping("/payment-complete")
    public String paymentCompletePage() {
        return "payment-complete";
    }

    @GetMapping("/refund")
    public String refundPage() {
        return "refund";
    }
}
