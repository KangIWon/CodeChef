package com.sparta.codechef;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CodeChefApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeChefApplication.class, args);
    }

}
