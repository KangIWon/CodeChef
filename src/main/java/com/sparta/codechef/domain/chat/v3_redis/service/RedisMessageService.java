package com.sparta.codechef.domain.chat.v3_redis.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.exception.ApiException;
import com.sparta.codechef.domain.chat.v3_redis.dto.MessageDto;
import com.sparta.codechef.domain.chat.v3_redis.dto.UnsubscribeDto;
import com.sparta.codechef.domain.chat.v3_redis.dto.response.MessageGetResponse;
import com.sparta.codechef.domain.chat.v3_redis.entity.ChatUser;
import com.sparta.codechef.domain.chat.v3_redis.entity.Message;
import com.sparta.codechef.domain.chat.v3_redis.repository.RedisChatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.sparta.codechef.domain.chat.v3_redis.enums.MessageType.*;
import static com.sparta.codechef.domain.chat.v3_redis.enums.RedisKey.ID_MESSAGE;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisMessageService {

    private final RedisChatRepository chatRepository;
    private final RedisChatService chatService;
    private final RedisPublisher redisPublisher;
    private final ObjectMapper objectMapper = new ObjectMapper();


    /**
     * 이전 메세지 로드
     * @param roomId : 채팅방 ID
     * @param userId : 유저 ID
     */
    public void getMessages(Long roomId, Long userId) {
        ChannelTopic initTopic = this.chatService.getInitTopic(roomId, userId);

        List<MessageGetResponse> messageList = this.chatRepository.findAllMessageGetResponseById(roomId);
        if (messageList.isEmpty()) {
            return;
        }

        this.chatService.addTopic(initTopic);
        messageList.stream()
                .filter(Objects::nonNull)
                .forEach(message -> this.redisPublisher.publish(initTopic, message));
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

        ChannelTopic topic = this.chatService.getTopic(roomId);
        String email = chatUser.getEmail();

        if (Objects.equals(hostId, chatUser.getId())) {
            this.chatService.addTopic(topic);

            Message message = Message.getMessage(
                    this.chatRepository.generateId(ID_MESSAGE),
                    CREATE,
                    roomId,
                    email
            );

            MessageDto savedMessage = this.chatRepository.saveMessage(message);
            this.redisPublisher.publish(topic, new MessageGetResponse(savedMessage));
            return;
        }

        Message message = Message.getMessage(
                this.chatRepository.generateId(ID_MESSAGE),
                IN,
                roomId,
                email
        );

        MessageDto savedMessage = this.chatRepository.saveMessage(message);
        this.redisPublisher.publish(topic, new MessageGetResponse(savedMessage));
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

            Message message = Message.getMessage(
                    this.chatRepository.generateId(ID_MESSAGE),
                    roomId,
                    chatUser,
                    content
            );

            ChannelTopic topic = this.chatService.getTopic(roomId);

            MessageDto messageDto = this.chatRepository.saveMessage(message);
            this.redisPublisher.publish(topic, new MessageGetResponse(messageDto));
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
        ChannelTopic topic = this.chatService.getTopic(roomId);
        ChannelTopic initTopic = this.chatService.getInitTopic(roomId, chatUser.getId());
        this.chatService.removeTopic(initTopic);

        boolean isSuccess = dto.isSuccess();
        Long nextHostId = dto.getNextHostId();
        if (isSuccess && nextHostId == null) {
            this.chatService.removeTopic(topic);;
            return;
        }

        List<MessageGetResponse> messageList = new ArrayList<>();

        Message outMessage = Message.getMessage(
                this.chatRepository.generateId(ID_MESSAGE),
                OUT,
                roomId,
                chatUser.getEmail()
        );
        MessageDto savedOutMessage = this.chatRepository.saveMessage(outMessage);
        messageList.add(new MessageGetResponse(savedOutMessage));

        if (isSuccess) {
            String nextHostEmail = this.chatRepository.findEmailById(nextHostId);
            Message successMessage = Message.getMessage(
                    this.chatRepository.generateId(ID_MESSAGE),
                    NEW_HOST,
                    roomId,
                    nextHostEmail
            );
            MessageDto savedSuccessMessage = this.chatRepository.saveMessage(successMessage);
            messageList.add(new MessageGetResponse(savedSuccessMessage));
        }

        this.redisPublisher.publish(topic, messageList);
    }
}
