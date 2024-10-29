package com.sparta.codechef.domain.chat.v2.entity;

import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.exception.ApiException;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@RedisHash("Message")
@Builder(builderClassName = "WSMessageBuilder")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WSMessage implements Serializable {
    @Id
    private String id;
    private Long topic;
    private Long sender;
    private String content;
    @Builder.Default
    private final LocalDateTime createdAt = LocalDateTime.now();

    public static class WSMessageBuilder {
        public WSMessageBuilder createdAt(LocalDateTime createdAt) {
            throw new ApiException(ErrorStatus.CREATED_AT_CANNOT_BE_SET);
        }
    }
}
