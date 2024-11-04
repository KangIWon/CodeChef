package com.sparta.codechef.domain.chat.v2.repository;

import com.sparta.codechef.domain.chat.v2.entity.WSMessage;
import org.springframework.data.repository.CrudRepository;

public interface WSMessageRepository extends CrudRepository<WSMessage, Long> {
}
