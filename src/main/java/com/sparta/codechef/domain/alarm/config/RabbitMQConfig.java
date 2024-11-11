package com.sparta.codechef.domain.alarm.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.BindingBuilder;

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
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory(host, port);
        factory.setUsername(username);
        factory.setPassword(password);
        return factory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        return new RabbitTemplate(connectionFactory());
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
