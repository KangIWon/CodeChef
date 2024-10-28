package com.sparta.codechef.domain.chat.service;

import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.exception.ApiException;
import com.sparta.codechef.domain.chat.dto.request.ChatRoomCreateRequest;
import com.sparta.codechef.domain.chat.dto.request.ChatRoomRequest;
import com.sparta.codechef.domain.chat.dto.response.ChatRoomGetResponse;
import com.sparta.codechef.domain.chat.dto.response.ChatRoomResponse;
import com.sparta.codechef.domain.chat.entity.ChatRoom;
import com.sparta.codechef.domain.chat.repository.chat_room.ChatRoomRepository;
import com.sparta.codechef.domain.user.entity.User;
import com.sparta.codechef.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatRoomServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ChatRoomService chatRoomService;

    @Nested
    class CreateRoomTest {
        @Test
        public void 유저ID로_유저를_찾지_못해_예외_발생() {
            // given
            Long userId = 1L;
            ChatRoomCreateRequest request = new ChatRoomCreateRequest("chatRoom", null, 10);

            given(userRepository.findById(anyLong())).willReturn(Optional.empty());

            // when & then
            ApiException exception = assertThrows(ApiException.class, () -> chatRoomService.createRoom(userId, request));
            assertEquals(ErrorStatus.NOT_FOUND_USER, exception.getErrorCode());
        }

        @Test
        public void 채팅방_생성_성공() {
            // given
            Long userId = 1L;
            ChatRoomCreateRequest request = new ChatRoomCreateRequest("채팅방", "123", 2);
            User user = spy(User.class);

            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
            given(chatRoomRepository.save(any(ChatRoom.class))).willReturn(spy(ChatRoom.class));

            // when
            ChatRoomResponse response = chatRoomService.createRoom(userId, request);

            // then
            verify(userRepository, times(1)).findById(anyLong());
            verify(chatRoomRepository, times(1)).save(any(ChatRoom.class));
            verify(user, times(1)).updateChatRoom(any(ChatRoom.class));
            assertInstanceOf(ChatRoomResponse.class, response);

        }
    }

    @Nested
    class GetSearchRoomTest {
        @Test
        public void 채팅방_전체_조회_성공() {
            // given
            int page = 0;
            int size = 10;
            Pageable pageable = PageRequest.of(page, size, Sort.by("id"));
            Page<ChatRoomGetResponse> chatRoomList = new PageImpl<>(List.of(spy(ChatRoomGetResponse.class), spy(ChatRoomGetResponse.class)), pageable, 2);

            given(chatRoomRepository.findAllChatRoom(any(Pageable.class))).willReturn(chatRoomList);

            // when
            Page<ChatRoomGetResponse> response = chatRoomService.getChatRooms(page, size);

            //then
            verify(chatRoomRepository, times(1)).findAllChatRoom(any(Pageable.class));
            assertInstanceOf(Page.class, response);
            response.getContent().forEach(item -> {
                assertInstanceOf(ChatRoomGetResponse.class, item);
            });
        }
    }

    @Nested
    class UpdateChatRoomTest{
        @Test
        public void 채팅방을_찾을_수_없어_실패() {
            // given
            Long userId = 1L;
            Long chatRoomId = 1L;
            ChatRoomRequest request = spy(ChatRoomRequest.class);

            given(chatRoomRepository.findByIdAndUser(anyLong(), anyLong())).willReturn(Optional.empty());

            // when & then
            ApiException exception = assertThrows(ApiException.class, () -> chatRoomService.updateChatRoom(chatRoomId, request));
            assertEquals(ErrorStatus.NOT_FOUND_CHATROOM, exception.getErrorCode());
        }

        @Test
        public void 채팅방_정보_수정_성공() {
            // given
            Long userId = 1L;
            Long chatRoomId = 1L;
            ChatRoomRequest request = new ChatRoomRequest("chatRoom", "123", 10);
            ChatRoom chatRoom = ChatRoom.builder()
                    .title("채팅방")
                    .password("123")
                    .maxParticipants(2)
                    .build();

            given(chatRoomRepository.findByIdAndUser(anyLong(), anyLong())).willReturn(Optional.of(chatRoom));
            chatRoom.updateRoomInfo(request.getTitle(), request.getPassword(), request.getMaxParticipants());
            given(chatRoomRepository.save(any(ChatRoom.class))).willReturn(chatRoom);

            // when
            ChatRoomResponse response = chatRoomService.updateChatRoom(chatRoomId, request);

            //then
            verify(chatRoomRepository, times(1)).findByIdAndUser(anyLong(), anyLong());
            verify(chatRoomRepository, times(1)).save(any(ChatRoom.class));
            assertInstanceOf(ChatRoomResponse.class, response);
            assertEquals(request.getTitle(), response.getTitle());
            assertEquals(request.getMaxParticipants(), response.getMaxParticipants());
        }
    }

    @Nested
    class EnterChatRoomTest {
        @Test
        public void 유저를_찾지_못해_예외_발생() {
            // given
            Long userId = 1L;
            Long chatRoomId = 1L;

            given(userRepository.findById(anyLong())).willReturn(Optional.empty());

            // when & then
            ApiException exception = assertThrows(ApiException.class, () -> chatRoomService.enterChatRoom(userId, chatRoomId));
            assertEquals(ErrorStatus.NOT_FOUND_USER, exception.getErrorCode());
        }

        @Test
        public void 유저가_이미_채팅방에_접속_중이어서_예외_발생() {
            // given
            Long userId = 1L;
            Long chatRoomId = 1L;
            User user = spy(User.class);
            ReflectionTestUtils.setField(user,"chatRoom", spy(ChatRoom.class));

            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

            // when & then
            ApiException exception = assertThrows(ApiException.class, () -> chatRoomService.enterChatRoom(userId, chatRoomId));
            assertEquals(ErrorStatus.ALREADY_IN_CHATROOM, exception.getErrorCode());
        }
    }
}
