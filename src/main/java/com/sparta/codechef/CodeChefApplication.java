package com.sparta.codechef;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableRetry // Spring Retry 활성화
public class CodeChefApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeChefApplication.class, args);
    }

}
