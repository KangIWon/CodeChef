package com.sparta.codechef.domain.chat.v3_redisPubSub.repository;

import com.sparta.codechef.domain.chat.v3_redisPubSub.dto.ChatUserDto;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RedisChatUserRepository extends CrudRepository<ChatUserDto, Long> {

}
