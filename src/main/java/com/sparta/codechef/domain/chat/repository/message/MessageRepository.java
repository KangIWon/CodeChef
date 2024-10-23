package com.sparta.codechef.domain.chat.repository.message;

import com.sparta.codechef.domain.chat.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long>, MessageQueryDslRepository {

}
