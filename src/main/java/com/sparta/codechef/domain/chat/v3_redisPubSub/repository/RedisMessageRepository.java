package com.sparta.codechef.domain.chat.v3_redisPubSub.repository;

import com.sparta.codechef.domain.chat.v3_redisPubSub.dto.MessageDto;
import org.springframework.data.repository.CrudRepository;

public interface RedisMessageRepository extends CrudRepository<MessageDto, Long> {
}
