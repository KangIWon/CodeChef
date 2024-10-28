package com.sparta.codechef.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.setCodec(new StringCodec()); // StringCodec으로 설정
        config.useSingleServer().setAddress("redis://127.0.0.1:6379"); // Redis 서버 주소 설정
        return Redisson.create(config);
    }
}