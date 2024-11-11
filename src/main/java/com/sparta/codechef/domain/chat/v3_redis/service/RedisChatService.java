package com.sparta.codechef.domain.chat.v3_redis.service;

import com.sparta.codechef.domain.chat.v3_redis.entity.ChatUser;
import com.sparta.codechef.domain.chat.v3_redis.enums.Subscribe;
import com.sparta.codechef.domain.chat.v3_redis.repository.RedisChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisChatService {
    private final RedisMessageListenerContainer container;
    private final MessageListenerAdapter messageListener;
    private final RedisChatRepository chatRepository;

    // 웹 소켓 연결 후, 채팅 유저 저장
    public void connectChatUser(ChatUser chatUser) {
        this.chatRepository.saveChatUser(chatUser);
    }

    // 웹 소켓 연결 해제 후, 채팅 유저 삭제
    public void disconnectChatUser(Long userId) {
        this.chatRepository.deleteChatUserById(userId);
    }


    /**
     * 컨테이너에 주제(topic) 추가
     * @param topic : 추가할 주제(topic)
     */
    public void addTopic(ChannelTopic topic) {
        container.addMessageListener(messageListener, topic);
    }

    /**
     * 컨테이너에서 주제(topic) 삭제
     * @param topic : 삭제할 주제(topic)
     */
    public void removeTopic(ChannelTopic topic) {
        container.removeMessageListener(messageListener, topic);
    }


    /**
     * 채팅방 주제(topic) Getter
     * @param roomId : 채팅방 ID
     * @return
     */
    public ChannelTopic getTopic(Long roomId) {
        return new ChannelTopic(
                new StringBuffer()
                        .append(Subscribe.TOPIC_PREFIX.getKey())
                        .append(Subscribe.CHAT_ROOM.getKey())
                        .append(roomId)
                        .toString()
        );
    }

    /**
     * 이전 메세지 로드용 주제(topic) Getter
     * @param userId : 유저 ID
     * @return
     */
    public ChannelTopic getInitTopic(Long roomId, Long userId) {
        return new ChannelTopic(
                new StringBuffer()
                        .append(Subscribe.TOPIC_PREFIX.getKey())
                        .append(Subscribe.CHAT_ROOM.getKey())
                        .append(roomId)
                        .append(Subscribe.INIT.getKey())
                        .append(userId)
                        .toString()
        );
    }
}
