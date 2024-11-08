package com.sparta.codechef.domain.chat.v3_redis.repository;

import com.sparta.codechef.domain.chat.v3_redis.dto.MessageDto;
import org.springframework.data.repository.CrudRepository;

public interface RedisMessageRepository extends CrudRepository<MessageDto, Long> {
}
