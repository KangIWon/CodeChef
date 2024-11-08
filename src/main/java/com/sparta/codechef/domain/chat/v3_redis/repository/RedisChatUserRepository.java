package com.sparta.codechef.domain.chat.v3_redis.repository;

import com.sparta.codechef.domain.chat.v3_redis.dto.ChatUserDto;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RedisChatUserRepository extends CrudRepository<ChatUserDto, Long> {

}
