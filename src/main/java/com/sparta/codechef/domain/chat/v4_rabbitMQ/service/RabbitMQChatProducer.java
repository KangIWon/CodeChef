package com.sparta.codechef.domain.chat.v4_rabbitMQ.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.exception.ApiException;
import com.sparta.codechef.domain.chat.v3_redisPubSub.dto.MessageDto;
import com.sparta.codechef.domain.chat.v3_redisPubSub.dto.response.MessageGetResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sparta.codechef.domain.chat.v3_redisPubSub.enums.DestinationKey.*;

@Slf4j
@Service
public class RabbitMQChatProducer {
    private final RabbitTemplate publishTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public RabbitMQChatProducer(@Qualifier("publishTemplate") RabbitTemplate publishTemplate) {
        this.publishTemplate = publishTemplate;
    }

    public void sendMessage(String exchange, String routingKey, Message message) {
        publishTemplate.convertAndSend(exchange, routingKey, message);
    }


    public Message getMessage(String destination, MessageGetResponse meesageObject) {
        try {
            byte[] messageBody = objectMapper.writeValueAsBytes(meesageObject);

            MessageProperties properties = new MessageProperties();
            properties.setHeader("destination", destination);
            properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);

            return MessageBuilder.withBody(messageBody)
                    .andProperties(properties)
                    .build();
        } catch (JsonProcessingException e) {
            throw new ApiException(ErrorStatus.JSON_CHANGE_FAILED);
        }
    }

    public String getChatRoomDestination(Long roomId) {
        return new StringBuffer()
                .append("/")
                .append(TOPIC_PREFIX.getKey())
                .append("/")
                .append(CHAT_ROOM.getKey())
                .append("/")
                .append(roomId)
                .toString();
    }

    public String getInitDestination(Long roomId, Long userId) {
        return new StringBuffer()
                .append(this.getChatRoomDestination(roomId))
                .append("/")
                .append(INIT.getKey())
                .append("/")
                .append(userId)
                .toString();
    }
}
