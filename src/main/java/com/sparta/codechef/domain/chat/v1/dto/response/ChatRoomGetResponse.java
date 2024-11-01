package com.sparta.codechef.domain.chat.v1.dto.response;

import com.sparta.codechef.domain.chat.v2.entity.WSChatRoom;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class ChatRoomGetResponse {
    private final Long id;
    private final String title;
    private final boolean isSecret;
    private final int curParticipants;
    private final int maxParticipants;

    public ChatRoomGetResponse(Long id, String title, String password, int curParticipants, int maxParticipants) {
        this.id = id;
        this.title = title;
        this.isSecret = password != null;
        this.curParticipants = curParticipants;
        this.maxParticipants = maxParticipants;
    }

    public ChatRoomGetResponse(Long id, String title, boolean isSecret, Long curParticipants, int maxParticipants) {
        this.id = id;
        this.title = title;
        this.isSecret = isSecret;
        this.curParticipants = curParticipants.intValue();
        this.maxParticipants = maxParticipants;
    }

    public ChatRoomGetResponse(WSChatRoom chatRoom) {
        this.id = chatRoom.getId();
        this.title = chatRoom.getTitle();
        this.isSecret = chatRoom.getPassword() != null;
        this.curParticipants = chatRoom.getCurParticipants();
        this.maxParticipants = chatRoom.getMaxParticipants();
    }
}
