package com.sparta.codechef.domain.alarm.config;

import com.fasterxml.jackson.databind.ser.std.StringSerializer;
import com.sparta.codechef.domain.chat.v3_redisPubSub.service.RedisSubscriber;
import com.sparta.codechef.domain.chat.v4_rabbitMQ.service.RabbitMQChatConsumer;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Configuration
public class RabbitMQConfig {

    @Value("${RABBITMQ_HOST}")
    private String host;

    @Value("${RABBITMQ_PORT}")
    private int port;

    @Value("${RABBITMQ_DEFAULT_USER}")
    private String username;

    @Value("${RABBITMQ_DEFAULT_PASS}")
    private String password;


    @Bean
    public ConnectionFactory alarmConnectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory(host, port);
        factory.setUsername(username);
        factory.setPassword(password);
        return factory;
    }

    // 발행용 채널
    @Bean
    public ConnectionFactory publishConnectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory(host, port);
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setChannelCacheSize(15); // 발행용 캐시 크기
        return factory;
    }

    @Bean
    public ConnectionFactory subscribeConnectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory(host, port);
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setChannelCacheSize(5);  // 수신용 캐시 크기
        factory.setRequestedHeartBeat(20); // 수신용에 적합한 설정 추가
        return factory;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory subscribeConnectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(subscribeConnectionFactory);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        return factory;
    }

    @Bean
    public DirectExchange chatDirectExchange(RabbitAdmin rabbitAdmin) {
        DirectExchange directExchange = new DirectExchange("chat.direct.exchange", true, false);
        rabbitAdmin.declareExchange(directExchange);
        return directExchange;
    }

    @Bean
    public DirectExchange chatInitDirectExchange(RabbitAdmin rabbitAdmin) {
        DirectExchange directExchange = new DirectExchange("chat.init.direct.exchange", true, false);
        rabbitAdmin.declareExchange(directExchange);
        return directExchange;
    }

    @Bean
    SimpleMessageListenerContainer container(ConnectionFactory subscribeConnectionFactory, MessageListenerAdapter messageListener) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(subscribeConnectionFactory);
        container.setMessageListener(messageListener);
        return container;
    }

    // 수신할 메서드 설정
    @Bean
    public MessageListenerAdapter messageListener(RabbitMQChatConsumer chatConsumer) {
        return new MessageListenerAdapter(chatConsumer, "onMessage");
    }


    @Bean
    public RabbitAdmin rabbitAdmin() {
        return new RabbitAdmin(publishConnectionFactory());
    }

    @Bean
    public RabbitTemplate publishTemplate(ConnectionFactory publishConnectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(publishConnectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public RabbitTemplate subscribeTemplate(ConnectionFactory publishConnectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(publishConnectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        return new RabbitTemplate(alarmConnectionFactory());
    }

    // 댓글 알림을 위한 Queue와 Exchange 설정
    @Bean
    public Queue commentQueue() {
        return new Queue("comment.notifications.queue", true);
    }

    @Bean
    public DirectExchange commentExchange() {
        return new DirectExchange("comment.notifications.exchange");
    }

    @Bean
    public Binding commentBinding(Queue commentQueue, DirectExchange commentExchange) {
        return BindingBuilder.bind(commentQueue).to(commentExchange).with("comment.notifications.key");
    }

    // 이벤트 알림을 위한 Queue와 Exchange 설정
    @Bean
    public Queue eventQueue() {
        return new Queue("event.notifications.queue", true);
    }

    @Bean
    public DirectExchange eventExchange() {
        return new DirectExchange("event.notifications.exchange");
    }

    @Bean
    public Binding eventBinding(Queue eventQueue, DirectExchange eventExchange) {
        return BindingBuilder.bind(eventQueue).to(eventExchange).with("event.notifications.key");
    }
}
