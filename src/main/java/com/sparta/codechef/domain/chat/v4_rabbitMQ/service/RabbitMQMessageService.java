package com.sparta.codechef.domain.chat.v4_rabbitMQ.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.exception.ApiException;
import com.sparta.codechef.domain.chat.v3_redisPubSub.dto.MessageDto;
import com.sparta.codechef.domain.chat.v3_redisPubSub.dto.UnsubscribeDto;
import com.sparta.codechef.domain.chat.v3_redisPubSub.dto.response.MessageGetResponse;
import com.sparta.codechef.domain.chat.v3_redisPubSub.entity.ChatMessage;
import com.sparta.codechef.domain.chat.v3_redisPubSub.entity.ChatUser;
import com.sparta.codechef.domain.chat.v3_redisPubSub.repository.RedisChatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.amqp.core.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.sparta.codechef.domain.chat.v3_redisPubSub.enums.MessageType.*;
import static com.sparta.codechef.domain.chat.v3_redisPubSub.enums.MessageType.NEW_HOST;
import static com.sparta.codechef.domain.chat.v3_redisPubSub.enums.RedisKey.ID_MESSAGE;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMQMessageService {

    private final RedisChatRepository chatRepository;
    private final RedisChatService chatRoomService;
    private final RabbitMQChatService chatService;
    private final RabbitMQChatProducer producer;
    private final DirectExchange chatDirectExchange;
    private final DirectExchange chatInitDirectExchange;
    private final ObjectMapper objectMapper = new ObjectMapper();


    /**
     * 이전 메세지 로드
     * @param payload : 페이징 정보
     * @param roomId : 채팅방 ID
     * @param userId : 유저 ID
     */
    public void getMessages(String payload, Long roomId, Long userId) {
        try {
            JsonNode node = objectMapper.readTree(payload);
            int page = node.get("page") == null ? 1 : node.get("page").asInt();
            int size = node.get("size") == null ? 10 : node.get("size").asInt();

            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "id"));
            Page<MessageDto> messageList = this.chatRepository.findAllMessageByRoomId(roomId, pageable);
            if (messageList.isEmpty()) {
                return;
            }
            log.info("load size : {}", messageList.getTotalElements());

            String destination = this.producer.getInitDestination(roomId, userId);
            messageList.forEach(messageDto ->
                    this.producer.sendMessage(
                            chatInitDirectExchange.getName(),
                            this.chatService.getInitRoutingKey(roomId),
                            this.producer.getMessage(destination, new MessageGetResponse(messageDto))
                    )
            );
        } catch (IOException e) {
            throw new ApiException(ErrorStatus.JSON_READ_FAILED);
        }
    }


    /**
     * 채팅방 입장(구독)
     * @param roomId : 채팅방 ID
     * @param chatUser : 채팅방 입장 유저
     */
    public void subscribeChatRoom(Long roomId, ChatUser chatUser) {
        Long hostId = this.chatRepository.findChatRoomByIDAndGetHostId(roomId);

        if (hostId == null) {
            throw new ApiException(ErrorStatus.NOT_FOUND_CHATROOM);
        }

        String routingKey = this.chatService.getChatRoutingKey(roomId);
        String destination = this.producer.getChatRoomDestination(roomId);
        String email = chatUser.getEmail();

        if (Objects.equals(hostId, chatUser.getId())) {
            this.chatService.addChatRoom(roomId);
            List<Message> messageList = new ArrayList<>();

            ChatMessage createMessage = ChatMessage.getMessage(
                    this.chatRepository.generateId(ID_MESSAGE),
                    CREATE,
                    roomId,
                    null
            );

            MessageDto savedCreateMessage = this.chatRepository.saveMessage(createMessage);
            Message createMSG = this.producer.getMessage(destination, new MessageGetResponse(savedCreateMessage));
            messageList.add(createMSG);

            ChatMessage newHostMessage = ChatMessage.getMessage(
                    this.chatRepository.generateId(ID_MESSAGE),
                    NEW_HOST,
                    roomId,
                    email
            );

            MessageDto savednewHostMessage = this.chatRepository.saveMessage(newHostMessage);
            Message newHostMSG = this.producer.getMessage(destination, new MessageGetResponse(savednewHostMessage));
            messageList.add(newHostMSG);

            messageList.forEach(message ->
                this.producer.sendMessage(
                        chatDirectExchange.getName(),
                        routingKey,
                        message
                )
            );
            return;
        }

        ChatMessage message = ChatMessage.getMessage(
                this.chatRepository.generateId(ID_MESSAGE),
                IN,
                roomId,
                email
        );

        MessageDto savedMessage = this.chatRepository.saveMessage(message);
        Message sendMessage = this.producer.getMessage(destination, new MessageGetResponse(savedMessage));
        this.producer.sendMessage(
                chatDirectExchange.getName(),
                routingKey,
                sendMessage
        );
    }


    /**
     * 채팅 메세지 전송
     * @param chatUser : 메세지 전송 유저
     * @param roomId : 채팅방 ID
     * @param content : 전송할 메세지 문자열
     */
    public void sendMessage(ChatUser chatUser, Long roomId, String content) {
        try {
            JsonNode node = objectMapper.readTree(content);
            content = node.get("content").asText();

            ChatMessage message = ChatMessage.getMessage(
                    this.chatRepository.generateId(ID_MESSAGE),
                    roomId,
                    chatUser,
                    content
            );

            MessageDto savedMessage = this.chatRepository.saveMessage(message);
            String destination = this.producer.getChatRoomDestination(roomId);
            Message sendMessage = this.producer.getMessage(destination, new MessageGetResponse(savedMessage));
            this.producer.sendMessage(
                    chatDirectExchange.getName(),
                    this.chatService.getChatRoutingKey(roomId),
                    sendMessage
            );
        } catch (JsonProcessingException e) {
            throw new ApiException(ErrorStatus.JSON_READ_FAILED);
        }
    }


    /**
     * 채팅방 퇴장(구독 취소)
     * @param roomId : 채팅방 ID
     * @param dto : 방장 승계 정보(success, nextHostId)
     * @param chatUser : 퇴장 유저 ID
     */
    public void unsubscribeChatRoom(Long roomId, UnsubscribeDto dto, ChatUser chatUser) {
        boolean isSuccess = dto.isSuccess();
        Long nextHostId = dto.getNextHostId();
        if (isSuccess && nextHostId == null) {
            this.chatService.removeChatRoom(roomId);
            return;
        }

        List<Message> messageList = new ArrayList<>();
        String destination = this.producer.getChatRoomDestination(roomId);

        ChatMessage outMessage = ChatMessage.getMessage(
                this.chatRepository.generateId(ID_MESSAGE),
                OUT,
                roomId,
                chatUser.getEmail()
        );
        MessageDto savedOutMessage = this.chatRepository.saveMessage(outMessage);
        messageList.add(this.producer.getMessage(destination, new MessageGetResponse(savedOutMessage)));

        if (isSuccess) {
            String nextHostEmail = this.chatRepository.findEmailById(nextHostId);
            ChatMessage successMessage = ChatMessage.getMessage(
                    this.chatRepository.generateId(ID_MESSAGE),
                    NEW_HOST,
                    roomId,
                    nextHostEmail
            );
            MessageDto savedSuccessMessage = this.chatRepository.saveMessage(successMessage);
            messageList.add(this.producer.getMessage(destination, new MessageGetResponse(savedSuccessMessage)));
        }

        messageList.forEach(message ->
                this.producer.sendMessage(
                        chatDirectExchange.getName(),
                        this.chatService.getChatRoutingKey(roomId),
                        message
                )
        );
    }
}
