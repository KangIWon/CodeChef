package com.sparta.codechef.domain.board.service;

import com.sparta.codechef.common.ApiResponse;
import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.enums.Framework;
import com.sparta.codechef.common.enums.Language;
import com.sparta.codechef.common.enums.Organization;
import com.sparta.codechef.common.enums.UserRole;
import com.sparta.codechef.common.exception.ApiException;
import com.sparta.codechef.domain.board.dto.request.BoardCreatedRequest;
import com.sparta.codechef.domain.board.dto.response.BoardDetailResponse;
import com.sparta.codechef.domain.board.dto.response.BoardResponse;
import com.sparta.codechef.domain.board.entity.Board;
import com.sparta.codechef.domain.board.repository.BoardRepository;
import com.sparta.codechef.domain.chat.entity.ChatRoom;
import com.sparta.codechef.domain.comment.entity.Comment;
import com.sparta.codechef.domain.user.entity.User;
import com.sparta.codechef.domain.user.repository.UserRepository;
import com.sparta.codechef.security.AuthUser;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

    @Mock
    private BoardRepository boardRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private BoardService boardService;

    @Nested
    class createBoard {
        @Test
        public void 유저_찾기_실패() {
            // given
            BoardCreatedRequest request = new BoardCreatedRequest();
            AuthUser authUser = new AuthUser(1L, "alden@naver.com", UserRole.ROLE_USER);
            given(userRepository.findById(anyLong())).willReturn(Optional.empty());
            // when
            ApiException apiException = assertThrows(ApiException.class,
                    () -> boardService.createBoard(request, authUser));
            // then
            assertEquals(ErrorStatus.NOT_FOUND_USER, apiException.getErrorCode()); // getErrorStatus()로 비교

        }

        @Test
        public void 게시물_등록_성공() {
            // given
            BoardCreatedRequest request = new BoardCreatedRequest();
            ReflectionTestUtils.setField(request, "title", "Sample Title");
            ReflectionTestUtils.setField(request, "contents", "Sample Content");
            ReflectionTestUtils.setField(request, "language", Language.JAVA);  // enum type
            ReflectionTestUtils.setField(request, "framework", Framework.SPRING);  // enum type

            AuthUser authUser = new AuthUser(1L, "alden@naver.com", UserRole.ROLE_USER);
            User user = new User();
            Board board = new Board();

            // Mocking the repository responses
            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
            given(boardRepository.save(any(Board.class))).willReturn(board);

            // when & then
            assertDoesNotThrow(() -> boardService.createBoard(request, authUser)); // 예외가 발생하지 않음을 확인
        }
    }

    @Nested
    class findAllBoard {

        @Test
        public void 모든_게시물_가져오기_성공() {
            // given
            int page = 1;
            int size = 10;
            Pageable pageable = PageRequest.of(page - 1, size);  // 실제 페이지는 0부터 시작하므로 그대로 사용

            // Mock된 User와 Board 데이터 생성
            User user = User.builder()
                    .id(1L)
                    .email("alden200@naver.com")
                    .password("123")
                    .personalHistory("3")
                    .userRole(UserRole.ROLE_ADMIN)
                    .organization(Organization.EMPLOYED)
                    .warning(5)
                    .isAttended(false)
                    .isDeleted(false)
                    .point(0)
                    .chatRoom(ChatRoom.builder()
                            .id(1L)
                            .title("123")
                            .password("123")
                            .maxParticipants(2)
                            .build())
                    .build();

            // Comment 데이터 설정
            Comment comment = new Comment();
            ReflectionTestUtils.setField(comment, "id", 1L);
            ReflectionTestUtils.setField(comment, "content", "First comment");

            Comment comment1 = new Comment();
            ReflectionTestUtils.setField(comment1, "id", 2L);
            ReflectionTestUtils.setField(comment1, "content", "Second comment");

            List<Comment> comments = new ArrayList<>();
            comments.add(comment);
            comments.add(comment1);

            // Board 데이터 설정
            Board board1 = Board.builder()
                    .id(1L)
                    .user(user)
                    .title("Test Title 1")
                    .contents("123")
                    .framework(Framework.SPRING)
                    .language(Language.JAVA)
                    .comments(comments)
                    .build();

            Board board2 = Board.builder()
                    .id(2L)  // board2의 id는 다르게 설정
                    .user(user)
                    .title("Another Title")
                    .contents("Another Content")
                    .framework(Framework.SPRING)
                    .language(Language.JAVA)
                    .comments(comments)
                    .build();

            List<Board> boardList = List.of(board1, board2);
            Page<Board> boardPage = new PageImpl<>(boardList, pageable, boardList.size());

            // when
            when(boardRepository.findAll(any(Pageable.class))).thenReturn(boardPage);

            // 실행
            Page<BoardResponse> boardResponses = boardService.findAllBoard(page, size);

            // then
            assertEquals(2, boardResponses.getContent().size());  // 반환된 게시물 개수 확인
            assertEquals("Test Title 1", boardResponses.getContent().get(0).getTitle());  // 첫 번째 게시물 제목 확인
            assertEquals("Java", boardResponses.getContent().get(0).getLanguage());  // 첫 번째 게시물 언어 확인
            assertEquals("SPRING", boardResponses.getContent().get(0).getFramework().name());  // 첫 번째 게시물 프레임워크 확인
        }
    }

    @Nested
    class getBoard {

        @Test
        public void 게시물_단건_조회_실패() {
            // given
           Long boardId = 1L;
            given(boardRepository.findById(anyLong())).willReturn(Optional.empty());
            // when
            ApiException apiException = assertThrows(ApiException.class,
                    () -> boardService.getBoard(boardId));
            // then
            assertEquals(ErrorStatus.NOT_FOUND_BOARD, apiException.getErrorCode()); // getErrorStatus()로 비교

        }

        @Test
        public void 게시물_단건_조회_성공() {
            // given
            User user = User.builder()
                    .id(1L)
                    .email("alden200@naver.com")
                    .password("123")
                    .personalHistory("3")
                    .userRole(UserRole.ROLE_ADMIN)
                    .organization(Organization.EMPLOYED)
                    .warning(5)
                    .isAttended(false)
                    .isDeleted(false)
                    .point(0)
                    .chatRoom(ChatRoom.builder()
                            .id(1L)
                            .title("123")
                            .password("123")
                            .maxParticipants(2)
                            .build())
                    .build();

            Board board = Board.builder()
                    .id(1L)
                    .user(user)
                    .title("Test Title 1")
                    .contents("123")
                    .framework(Framework.SPRING)
                    .language(Language.JAVA)
                    .comments(null)
                    .build();

            Comment comment = Comment.builder()
                    .id(1L)
                    .content("123")
                    .user(user)
                    .board(board)
                    .isAdopted(false).build();

            Comment comment2 = Comment.builder()
                    .id(1L)
                    .content("123")
                    .user(user)
                    .board(board)
                    .isAdopted(false).build();

            List<Comment> comments = new ArrayList<>();
            comments.add(comment);
            comments.add(comment2);

            Board board2 =Board.builder()
                    .id(1L)
                    .user(user)
                    .title("Test Title 1")
                    .contents("123")
                    .framework(Framework.SPRING)
                    .language(Language.JAVA)
                    .comments(comments)
                    .build();



            Long boardId = 1L;
            given(boardRepository.findById(anyLong())).willReturn(Optional.of(board2));
            //when
            BoardDetailResponse response = boardService.getBoard(boardId);

            // then
            assertNotNull(response); // 기본적인 응답 확인
        }
    }
}
