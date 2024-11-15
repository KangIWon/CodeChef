package com.sparta.codechef.domain.chat.v4_rabbitMQ.service;

import com.sparta.codechef.domain.chat.v3_redisPubSub.entity.ChatUser;
import com.sparta.codechef.domain.chat.v3_redisPubSub.enums.DestinationKey;
import com.sparta.codechef.domain.chat.v3_redisPubSub.repository.RedisChatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMQChatService {

    private final SimpleMessageListenerContainer container;
    private final MessageListenerAdapter messageListener;
    private final RabbitTemplate publishTemplate;
    private final RedisChatRepository chatRepository;
    private final RabbitAdmin rabbitAdmin;
    private final DirectExchange chatDirectExchange;
    private final DirectExchange chatInitDirectExchange;

    private final String CHAT = "chat.";
    private final String ROOM = "room.";
    private final String INIT = "init.";
    private final String BINDING = "binding.";



    /**
     * 채팅방 생성
     *   - 채팅방 전용 큐 생성
     *   - 채팅방 전용 Exchange 생성 : Direct Exchange
     *   - 채팅방 큐와 채팅방 Exchange 바인딩
     * @param roomId : 채팅방 ID
     */
    public void addChatRoom(Long roomId) {
        log.info("create chat room: {}", roomId);
        Binding chatBinding = this.declareChatBinding(roomId);
        this.rabbitAdmin.declareBinding(chatBinding);


        Binding initBinding = this.declareInitBinding(roomId);
        this.rabbitAdmin.declareBinding(initBinding);
        this.container.addQueueNames(
                this.getRoomQName(roomId),
                this.getInitQName(roomId)
        );
    }

    /**
     * 채팅방 삭제
     *   - 채팅방 Direct Exchange 삭제
     * @param roomId : 채팅방 ID
     */
    public void removeChatRoom(Long roomId) {
        log.info("chat room {} is deleted", roomId);
        Binding chatBinding = this.getBinding(
                new Queue(this.getRoomQName(roomId)),
                this.chatDirectExchange,
                this.getChatRoutingKey(roomId)
        );
        this.rabbitAdmin.removeBinding(chatBinding);

        Binding initBinding = this.getBinding(
                new Queue(this.getInitQName(roomId)),
                this.chatInitDirectExchange,
                this.getInitRoutingKey(roomId)
        );
        this.rabbitAdmin.removeBinding(initBinding);
        this.container.removeQueueNames(
                this.getRoomQName(roomId),
                this.getInitQName(roomId)
        );
    }

    /**
     * 채팅방 큐 이름 Getter
     * @param roomId : 채팅방 ID
     * @return
     */
    public String getRoomQName(Long roomId) {
        return new StringBuffer()
                .append(CHAT)
                .append(ROOM)
                .append(roomId)
                .toString();
    }

    /**
     * 채팅방 이전 메세지 불러오기 용 큐 이름 Getter
     * @param roomId : 채팅방 ID
     * @return
     */
    public String getInitQName(Long roomId) {
        return new StringBuffer()
                .append(CHAT)
                .append(ROOM)
                .append(INIT)
                .append(roomId)
                .toString();
    }

    /**
     * 채팅방 큐 선언
     * @param roomId : 채팅방 ID
     * @return
     */
    private Queue declareChatQueue(Long roomId) {
        String queueName = this.getRoomQName(roomId);
        Map<String, Object> args = new HashMap<>(Map.of(
                "x-message-ttl", 60000
        ));

        Queue chatQueue = new Queue(queueName, true, false, true, args);
        this.rabbitAdmin.declareQueue(chatQueue);
        return chatQueue;
    }

    /**
     * 채팅방 이전 메세지 불러오기 용 큐 선언
     * @param roomId : 채팅방 ID
     * @return
     */
    private Queue declareInitQueue(Long roomId) {
        String queueName = this.getInitQName(roomId);
        Map<String, Object> args = new HashMap<>(Map.of(
                "x-message-ttl", 60000
        ));

        Queue initQueue = new Queue(queueName, true, false, true, args);
        this.rabbitAdmin.declareQueue(initQueue);
        return initQueue;
    }


    /**
     * 채팅방 routingKey Getter
     * @param roomId : 채팅방 ID
     * @return
     */
    public String getChatRoutingKey(Long roomId) {
        return new StringBuffer()
                .append(chatDirectExchange.getName())
                .append(".")
                .append(this.getRoomQName(roomId))
                .append(".")
                .append(BINDING)
                .toString();
    }

    /**
     * 채팅방 이전 메세지 불러오기 용 routingKey Getter
     * @param roomId : 채팅방 ID
     * @return
     */
    public String getInitRoutingKey(Long roomId) {
        return new StringBuffer()
                .append(chatInitDirectExchange.getName())
                .append(".")
                .append(this.getInitQName(roomId))
                .append(".")
                .append(BINDING)
                .toString();
    }

    /**
     * 채팅방 Binding 선언
     * @param roomId : 채팅방 ID
     * @return
     */
    private Binding declareChatBinding(Long roomId){
        Binding chatBinding = this.getBinding(
                this.declareChatQueue(roomId),
                chatDirectExchange,
                this.getChatRoutingKey(roomId)
        );
        this.rabbitAdmin.declareBinding(chatBinding);
        return chatBinding;
    }

    /**
     * 채팅방 이전 메세지 불러오기 용 Binding 선언
     * @param roomId : 채팅방 ID
     * @return
     */
    private Binding declareInitBinding(Long roomId) {
        Binding initBinding = this.getBinding(
                this.declareInitQueue(roomId),
                chatInitDirectExchange,
                this.getInitRoutingKey(roomId)
        );

        this.rabbitAdmin.declareBinding(initBinding);
        return initBinding;
    }

    /**
     * 바인딩 Getter
     * @param queue
     * @param exchange
     * @param routingKey
     * @return
     */
    private Binding getBinding(Queue queue, Exchange exchange, String routingKey) {
        return BindingBuilder.bind(queue)
                .to(exchange)
                .with(routingKey).noargs();
    }

    // 웹 소켓 연결 후, 채팅 유저 저장
    public void connectChatUser(ChatUser chatUser) {
        this.chatRepository.saveChatUser(chatUser);
    }

    // 웹 소켓 연결 해제 후, 채팅 유저 삭제
    public void disconnectChatUser(Long userId) {
        this.chatRepository.deleteChatUserById(userId);
    }
}
