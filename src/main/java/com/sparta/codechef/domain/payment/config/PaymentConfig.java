package com.sparta.codechef.domain.payment.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class PaymentConfig {

    @Value("${payment.toss.test_client_api_key}")
    private String clientKey;

    @Value("${payment.toss.test_secret_api_key}")
    private String secretKey;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public String clientKey() {
        return clientKey;
    }

    @Bean
    public String secretKey() {
        return secretKey;
    }
}
