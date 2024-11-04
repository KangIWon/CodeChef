package com.sparta.codechef.domain.chat.v2.repository;

import com.sparta.codechef.domain.chat.v2.entity.WSChatRoom;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WSChatRoomRepository extends CrudRepository<WSChatRoom, Long> {

}
