package com.sparta.codechef.domain.chat.v2.service;

import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.enums.UserRole;
import com.sparta.codechef.common.exception.ApiException;
import com.sparta.codechef.domain.chat.v1.dto.request.ChatRoomCreateRequest;
import com.sparta.codechef.domain.chat.v1.dto.request.ChatRoomRequest;
import com.sparta.codechef.domain.chat.v1.dto.response.ChatRoomGetResponse;
import com.sparta.codechef.domain.chat.v1.dto.response.ChatRoomResponse;
import com.sparta.codechef.domain.chat.v2.entity.WSChatUser;
import com.sparta.codechef.domain.chat.v2.entity.WSChatUserRole;
import com.sparta.codechef.domain.chat.v2.entity.WSChatRoom;
import com.sparta.codechef.domain.chat.v2.repository.WSChatRoomRepository;
import com.sparta.codechef.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WSChatService {
    private final WSChatRoomRepository WSChatRoomRepository;
    private final com.sparta.codechef.domain.chat.v2.repository.WSChatUserRepository WSChatUserRepository;

    /**
     * 채팅방 생성
     * @param authUser : 인증 유저
     * @param request : 채팅방 생성 정보 DTO(채팅방 이름, 비밀번호, 최대 정원)
     * @return
     */
    public ChatRoomResponse createRoom(AuthUser authUser, ChatRoomCreateRequest request) {
        WSChatUser chatUser = this.WSChatUserRepository.findById(authUser.getUserId()).orElseThrow(
                () -> new ApiException(ErrorStatus.NOT_FOUND_CHAT_USER)
        );

        WSChatRoom chatRoom = WSChatRoom.builder()
                .id(this.WSChatUserRepository.generateId("chatRoom"))
                .title(request.getTitle())
                .maxParticipants(request.getMaxParticipants())
                .password(request.getPassword())
                .hostId(authUser.getUserId())
                .build();

        chatUser.updateChatRoom(chatRoom.getId());
        chatUser.updateRole(WSChatUserRole.ROLE_HOST);

        this.WSChatRoomRepository.save(chatRoom);
        this.WSChatUserRepository.save(chatUser);
        this.WSChatUserRepository.saveChatRoom(chatRoom);

        return new ChatRoomResponse(chatRoom);
    }

    /**
     * 채팅방 전체 조회 (with 페이징)
     * @param page : 페이지 번호
     * @param size : 페이지 크기
     * @return
     */
    public Page<ChatRoomGetResponse> getChatRooms(int page, int size) {
        return this.WSChatUserRepository.getAllChatRooms(page, size);
    }


    /**
     * 채팅방 정보 수정
     * @param chatRoomId : 채팅방 id
     * @param request : 채팅방 이름, 비밀번호, 최대 정원 정보가 담긴 DTO
     * @return
     */
    public ChatRoomResponse updateChatRoom(Long chatRoomId, ChatRoomRequest request) {
        WSChatRoom chatRoom = this.WSChatRoomRepository.findById(chatRoomId).orElseThrow(
                () -> new ApiException(ErrorStatus.NOT_FOUND_CHATROOM)
        );

        chatRoom.updateRoomInfo(
                request.getTitle(),
                request.getPassword(),
                request.getMaxParticipants()
        );

        this.WSChatRoomRepository.save(chatRoom);

        return new ChatRoomResponse(chatRoom);
    }


    /**
     * 채팅방 입장
     * @param chatRoomId : 채팅방 id
     * @param userId : 유저 id
     */
    public void subscribeChatRoom(Long chatRoomId, Long userId) {
        WSChatUser WSChatUser = this.WSChatUserRepository.findById(userId).orElseThrow(
                () -> new ApiException(ErrorStatus.NOT_FOUND_CHAT_USER)
        );

        if (WSChatUser.getChatRoomId() != null) {
            throw new ApiException(ErrorStatus.ALREADY_IN_CHATROOM);
        }

        WSChatRoom chatRoom = this.WSChatRoomRepository.findById(chatRoomId).orElseThrow(
                () -> new ApiException(ErrorStatus.NOT_FOUND_CHATROOM)
        );

        Long curParticipants = this.WSChatUserRepository.countJoinedUser(chatRoomId);

        if (chatRoom.getMaxParticipants() > curParticipants) {
            WSChatUser.updateChatRoom(chatRoomId);
            this.WSChatUserRepository.save(WSChatUser);
            this.WSChatUserRepository.subscribeChatRoom(chatRoomId, userId);
        } else {
            throw new ApiException(ErrorStatus.ROOM_CAPACITY_EXCEEDED);
        }
    }

    /**
     * 채팅방 퇴장
     * @param chatRoomId : 채팅방 id
     * @param userId : 유저 id
     */
    public ChatRoomResponse unsubscribeChatRoom(Long chatRoomId, Long userId) {
        ChatRoomResponse response = null;
        WSChatUser chatUser = this.WSChatUserRepository.findById(userId).orElseThrow(
                () -> new ApiException(ErrorStatus.NOT_FOUND_CHAT_USER)
        );

        WSChatRoom chatRoom = this.WSChatRoomRepository.findById(chatRoomId).orElseThrow(
                () -> new ApiException(ErrorStatus.NOT_FOUND_CHATROOM)
        );

        boolean isHost = chatRoom.getHostId().equals(userId);

        if (isHost) {
            Optional<WSChatUser> optionalNewHost = this.WSChatUserRepository.findNextHost(chatRoomId, userId);

            if (optionalNewHost.isPresent()) {
                WSChatUser newHost = optionalNewHost.get();

                chatRoom.updateHost(newHost.getId());
                newHost.updateRole(WSChatUserRole.ROLE_HOST);

                this.WSChatRoomRepository.save(chatRoom);
                this.WSChatUserRepository.save(newHost);

                response = new ChatRoomResponse(chatRoom);
            } else {
                this.WSChatRoomRepository.delete(chatRoom);
                this.WSChatUserRepository.deleteFromChatRoomList(chatRoomId);

                // 채팅 메세지 정리
            }
        }

        chatUser.updateChatRoom(null);
        chatUser.updateRole(WSChatUserRole.ROLE_USER);
        this.WSChatUserRepository.save(chatUser);

        return response;
    }

    // 웹 소켓 연결
    public void connectChatUser(WSChatUser chatUser) {
        this.WSChatUserRepository.save(chatUser);
    }

    // 웹 소켓 연결 해제
    public void disconnectChatUser(WSChatUser chatUser) {
        this.WSChatUserRepository.delete(chatUser);
    }


    // 방장 권한 체크용
    public boolean hasAccess(AuthUser authUser) {
        boolean isAdmin = authUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals(UserRole.ROLE_ADMIN.name()));

        WSChatUser WSChatUser = this.WSChatUserRepository.findById(authUser.getUserId()).orElse(null);
        boolean isHost = WSChatUser != null && WSChatUser.getRole().equals(WSChatUserRole.ROLE_HOST);


        if (!isHost && !isAdmin) {
            throw new ApiException(ErrorStatus.NOT_CHATROOM_HOST);
        }

        return true;
    }
}
