package com.sparta.codechef.domain.alarm.config;

import com.sparta.codechef.domain.alarm.service.CommentNotificationSubscriber;
import com.sparta.codechef.domain.alarm.service.EventNotificationSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
public class RedisSubscriberConfig {

    @Bean
    public RedisMessageListenerContainer redisContainer(RedisConnectionFactory connectionFactory,
                                                        MessageListenerAdapter commentListenerAdapter,
                                                        MessageListenerAdapter eventListenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        // 기존 댓글 알림 채널 구독
        container.addMessageListener(commentListenerAdapter, new ChannelTopic("commentNotifications"));

        // 새로운 이벤트 알림 채널 구독
        container.addMessageListener(eventListenerAdapter, new ChannelTopic("eventNotifications"));

        return container;
    }

    @Bean
    public MessageListenerAdapter commentListenerAdapter(CommentNotificationSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber);
    }

    @Bean
    public MessageListenerAdapter eventListenerAdapter(EventNotificationSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber);
    }
}
