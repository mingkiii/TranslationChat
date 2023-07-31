package com.example.translationchat.client.service;

import static com.example.translationchat.common.exception.ErrorCode.ALREADY_OPPONENT_REQUEST;
import static com.example.translationchat.common.exception.ErrorCode.ALREADY_REGISTERED_FRIEND;
import static com.example.translationchat.common.exception.ErrorCode.ALREADY_REQUEST_FRIENDSHIP;
import static com.example.translationchat.common.exception.ErrorCode.CAN_NOT_FRIEND_YOURSELF;
import static com.example.translationchat.common.exception.ErrorCode.FRIENDSHIP_STATUS_IS_BLOCKED;
import static com.example.translationchat.common.exception.ErrorCode.FRIENDSHIP_STATUS_IS_NOT_BLOCKED;
import static com.example.translationchat.common.exception.ErrorCode.NOT_FOUND_FRIENDSHIP;
import static com.example.translationchat.common.exception.ErrorCode.NOT_FOUND_USER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.example.translationchat.client.domain.dto.FriendInfoDto;
import com.example.translationchat.client.domain.model.Friendship;
import com.example.translationchat.client.domain.model.User;
import com.example.translationchat.client.domain.repository.FriendshipRepository;
import com.example.translationchat.client.domain.repository.UserRepository;
import com.example.translationchat.client.domain.type.FriendshipStatus;
import com.example.translationchat.client.domain.type.Language;
import com.example.translationchat.client.domain.type.Nationality;
import com.example.translationchat.common.exception.CustomException;
import com.example.translationchat.common.security.principal.PrincipalDetails;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@SpringBootTest
class FriendServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FriendshipRepository friendshipRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private FriendService friendService;

    // 친구 요청
    @Test
    @DisplayName("친구 요청 - 성공")
    public void testRequestFriend() {
        // given
        User user = User.builder()
            .id(1L)
            .name("user")
            .email("user1@example.com")
            .build();
        User friend = User.builder()
            .id(1L)
            .name("friend")
            .email("friend@example.com")
            .build();

        when(userRepository.findByName(anyString())).thenReturn(Optional.of(friend));
        when(friendshipRepository.findByUserAndFriend(
            user, friend)).thenReturn(Optional.empty());
        when(friendshipRepository.findByUserAndFriend(
            friend, user)).thenReturn(Optional.empty());
        // when
        String result = friendService.requestFriend(
            createMockAuthentication(user), friend.getName());

        // then
        assertEquals("friend 님에게 친구 요청하였습니다.", result);
    }
    @Test
    @DisplayName("친구 요청 - 실패_유저 자신에게 친구요청")
    public void testRequestFriend_Fail_CAN_NOT_FRIEND_YOURSELF() {
        // given
        String friendName = "user";
        User user = User.builder()
            .id(1L)
            .name("user")
            .email("user1@example.com")
            .build();

        // when
        CustomException exception = assertThrows(CustomException.class,
            () -> friendService.requestFriend(createMockAuthentication(user), friendName));

        // then
        assertEquals(CAN_NOT_FRIEND_YOURSELF, exception.getErrorCode());
    }
    @Test
    @DisplayName("친구 요청 - 실패_친구 찾을 수 없음")
    public void testRequestFriend_Fail_NOT_FOUND_USER() {
        // given
        String friendName = "friend";
        User user = User.builder()
            .id(1L)
            .name("user")
            .email("user1@example.com")
            .build();

        when(userRepository.findByName(anyString())).thenReturn(Optional.empty());
        // when
        CustomException exception = assertThrows(CustomException.class,
            () -> friendService.requestFriend(createMockAuthentication(user), friendName));

        // then
        assertEquals(NOT_FOUND_USER, exception.getErrorCode());
    }
    @Test
    @DisplayName("친구 요청 - 실패_친구가 이미 유저에게 요청한 경우")
    public void testRequestFriend_Fail_ALREADY_OPPONENT_REQUEST() {
        // given
        User user = User.builder()
            .id(1L)
            .name("user")
            .email("user1@example.com")
            .build();
        User friend = User.builder()
            .id(1L)
            .name("friend")
            .email("friend@example.com")
            .build();
        Friendship friendFriendship = Friendship.builder()
            .user(friend)
            .friend(user)
            .friendshipStatus(FriendshipStatus.PENDING)
            .build();

        when(userRepository.findByName(anyString())).thenReturn(Optional.of(friend));
        when(friendshipRepository.findByUserAndFriend(
            any(User.class), any(User.class))).thenReturn(Optional.of(friendFriendship));
        // when
        CustomException exception = assertThrows(CustomException.class,
            () -> friendService.requestFriend(createMockAuthentication(user), friend.getName()));

        // then
        assertEquals(ALREADY_OPPONENT_REQUEST, exception.getErrorCode());
    }
    @Test
    @DisplayName("친구 요청 - 실패_이미 친구 상태")
    public void testRequestFriend_Fail_ALREADY_REGISTERED_FRIENDSHIP() {
        // given
        String friendName = "friend";
        User user = User.builder()
            .id(1L)
            .name("user")
            .email("user1@example.com")
            .build();
        User friend = User.builder()
            .id(1L)
            .name("friend")
            .email("friend@example.com")
            .build();
        Friendship userFriendship = Friendship.builder()
            .user(user)
            .friend(friend)
            .friendshipStatus(FriendshipStatus.ACCEPTED)
            .build();

        when(userRepository.findByName(anyString())).thenReturn(Optional.of(friend));
        when(friendshipRepository.findByUserAndFriend(
            any(User.class), any(User.class))).thenReturn(Optional.of(userFriendship));
        // when
        CustomException exception = assertThrows(CustomException.class,
            () -> friendService.requestFriend(createMockAuthentication(user), friendName));

        // then
        assertEquals(ALREADY_REGISTERED_FRIEND, exception.getErrorCode());
    }
    @Test
    @DisplayName("친구 요청 - 실패_이미 요청 중인 상태")
    public void testRequestFriend_Fail_ALREADY_REQUEST_FRIENDSHIP() {
        // given
        String friendName = "friend";
        User user = User.builder()
            .id(1L)
            .name("user")
            .email("user1@example.com")
            .build();
        User friend = User.builder()
            .id(1L)
            .name("friend")
            .email("friend@example.com")
            .build();
        Friendship userFriendship = Friendship.builder()
            .user(user)
            .friend(friend)
            .friendshipStatus(FriendshipStatus.PENDING)
            .requestTime(LocalDateTime.now().minusDays(10)) // 10일 전에 친구요청한 상태
            .build();

        when(userRepository.findByName(anyString())).thenReturn(Optional.of(friend));
        when(friendshipRepository.findByUserAndFriend(
            user, friend)).thenReturn(Optional.of(userFriendship));
        when(friendshipRepository.findByUserAndFriend(
            friend, user)).thenReturn(Optional.empty());
        // when
        CustomException exception = assertThrows(CustomException.class,
            () -> friendService.requestFriend(createMockAuthentication(user), friendName));

        // then
        assertEquals(ALREADY_REQUEST_FRIENDSHIP, exception.getErrorCode());
    }
    @Test
    @DisplayName("친구 요청 - 실패_유저가 차단한 친구")
    public void testRequestFriend_Fail_FRIENDSHIP_STATUS_IS_BLOCKED() {
        // given
        String friendName = "friend";
        User user = User.builder()
            .id(1L)
            .name("user")
            .email("user1@example.com")
            .build();
        User friend = User.builder()
            .id(1L)
            .name("friend")
            .email("friend@example.com")
            .build();
        Friendship userFriendship = Friendship.builder()
            .user(user)
            .friend(friend)
            .friendshipStatus(FriendshipStatus.BLOCKED)
            .build();

        when(userRepository.findByName(anyString())).thenReturn(Optional.of(friend));
        when(friendshipRepository.findByUserAndFriend(
            any(User.class), any(User.class))).thenReturn(Optional.of(userFriendship));
        // when
        CustomException exception = assertThrows(CustomException.class,
            () -> friendService.requestFriend(createMockAuthentication(user), friendName));

        // then
        assertEquals(FRIENDSHIP_STATUS_IS_BLOCKED, exception.getErrorCode());
    }
    // 친구 요청 수락
    @Test
    @DisplayName("친구 요청 수락 - 성공")
    public void testAcceptFriendship() {
        // given
        Long notificationId = 1L;
        User user = User.builder()
            .id(1L)
            .name("user")
            .email("user1@example.com")
            .build();
        User requester = User.builder()
            .id(1L)
            .name("requester")
            .email("requester@example.com")
            .build();

        when(userRepository.findByName(anyString())).thenReturn(Optional.of(requester));
        Friendship requesterFriendship = Friendship.builder()
            .user(requester)
            .friend(user)
            .friendshipStatus(FriendshipStatus.PENDING)
            .build();
        when(friendshipRepository.findByUserAndFriend(user, requester)).thenReturn(Optional.empty());
        when(friendshipRepository.findByUserAndFriend(requester,user)).thenReturn(Optional.of(requesterFriendship));
        // when
        String result = friendService.acceptFriendship(
            createMockAuthentication(user), notificationId, requester.getName());

        // then
        assertEquals("requester 님과 친구가 되었습니다.", result);
    }
    @Test
    @DisplayName("친구 요청 수락 - 실패_요청자 찾을 수 없음")
    public void testAcceptFriendship_Fail_NOT_FOUND_USER() {
        // given
        Long notificationId = 1L;
        String requester = "requester";
        User user = User.builder()
            .id(1L)
            .name("user")
            .email("user1@example.com")
            .build();

        when(userRepository.findByName(requester)).thenReturn(Optional.empty());
        // when
        CustomException exception = assertThrows(CustomException.class,
            () ->  friendService.acceptFriendship(createMockAuthentication(user), notificationId, requester));

        // then
        assertEquals(NOT_FOUND_USER, exception.getErrorCode());
    }
    @Test
    @DisplayName("친구 요청 수락 - 실패_요청자의 요청기록 없음")
    public void testAcceptFriendship_Fail_NOT_FOUND_FRIENDSHIP() {
        // given
        Long notificationId = 1L;
        User user = User.builder()
            .id(1L)
            .name("user")
            .email("user1@example.com")
            .build();
        User requester = User.builder()
            .id(1L)
            .name("requester")
            .email("requester@example.com")
            .build();

        when(userRepository.findByName(requester.getName())).thenReturn(Optional.of(requester));
        when(friendshipRepository.findByUserAndFriend(requester, user)).thenReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class,
            () ->  friendService.acceptFriendship(createMockAuthentication(user), notificationId, requester.getName()));

        // then
        assertEquals(NOT_FOUND_FRIENDSHIP, exception.getErrorCode());
    }
    @Test
    @DisplayName("친구 요청 수락 - 실패_이미 친구 관계")
    public void testAcceptFriendship_Fail_ALREADY_REGISTERED_FRIENDSHIP() {
        // given
        Long notificationId = 1L;
        User user = User.builder()
            .id(1L)
            .name("user")
            .email("user1@example.com")
            .build();
        User requester = User.builder()
            .id(1L)
            .name("requester")
            .email("requester@example.com")
            .build();

        when(userRepository.findByName(requester.getName())).thenReturn(Optional.of(requester));
        Friendship userFriendship = Friendship.builder()
            .user(requester)
            .friend(user)
            .friendshipStatus(FriendshipStatus.ACCEPTED)
            .build();
        Friendship requesterFriendship = Friendship.builder()
            .user(requester)
            .friend(user)
            .friendshipStatus(FriendshipStatus.ACCEPTED)
            .build();
        when(friendshipRepository.findByUserAndFriend(user, requester)).thenReturn(Optional.of(userFriendship));
        when(friendshipRepository.findByUserAndFriend(requester,user)).thenReturn(Optional.of(requesterFriendship));
        // when
        CustomException exception = assertThrows(CustomException.class,
            () ->  friendService.acceptFriendship(createMockAuthentication(user), notificationId, requester.getName()));

        // then
        assertEquals(ALREADY_REGISTERED_FRIEND, exception.getErrorCode());
    }
    @Test
    @DisplayName("친구 요청 수락 - 실패_유저가 요청자를 차단한 경우")
    public void testAcceptFriendship_Fail_FRIENDSHIP_STATUS_IS_BLOCKED() {
        // given
        Long notificationId = 1L;
        User user = User.builder()
            .id(1L)
            .name("user")
            .email("user1@example.com")
            .build();
        User requester = User.builder()
            .id(1L)
            .name("requester")
            .email("requester@example.com")
            .build();

        when(userRepository.findByName(requester.getName())).thenReturn(Optional.of(requester));
        Friendship userFriendship = Friendship.builder()
            .user(requester)
            .friend(user)
            .friendshipStatus(FriendshipStatus.BLOCKED)
            .build();
        Friendship requesterFriendship = Friendship.builder()
            .user(requester)
            .friend(user)
            .friendshipStatus(FriendshipStatus.PENDING)
            .build();
        when(friendshipRepository.findByUserAndFriend(user, requester)).thenReturn(Optional.of(userFriendship));
        when(friendshipRepository.findByUserAndFriend(requester,user)).thenReturn(Optional.of(requesterFriendship));
        // when
        CustomException exception = assertThrows(CustomException.class,
            () ->  friendService.acceptFriendship(createMockAuthentication(user), notificationId, requester.getName()));

        // then
        assertEquals(FRIENDSHIP_STATUS_IS_BLOCKED, exception.getErrorCode());
    }

    // 친구 요청 거절
    @Test
    @DisplayName("친구 요청 거절 - 성공")
    public void testRefuseFriendship() {
        // given
        Long notificationId = 1L;
        User user = User.builder()
            .id(1L)
            .name("user")
            .email("user1@example.com")
            .build();
        User requester = User.builder()
            .id(1L)
            .name("requester")
            .email("requester@example.com")
            .build();
        Friendship requesterFriendship = Friendship.builder()
            .user(requester)
            .friend(user)
            .friendshipStatus(FriendshipStatus.PENDING)
            .build();

        when(userRepository.findByName(anyString())).thenReturn(Optional.of(requester));
        when(friendshipRepository.findByUserAndFriend(
            requester, user)).thenReturn(Optional.of(requesterFriendship));
        when(friendshipRepository.findByUserAndFriend(
            user, requester)).thenReturn(Optional.empty());
        // when
        String result = friendService.refuseFriendship(createMockAuthentication(user), notificationId, requester.getName());

        // then
        assertEquals("requester 님의 친구 요청을 거절했습니다.", result);
    }
    @Test
    @DisplayName("친구 요청 거절 - 실패_요청자 찾을 수 없음")
    public void testRefuseFriendship_Fail_NOT_FOUND_USER() {
        // given
        Long notificationId = 1L;
        String requester = "requester";
        User user = User.builder()
            .id(1L)
            .name("user")
            .email("user1@example.com")
            .build();

        when(userRepository.findByName(requester)).thenReturn(Optional.empty());
        // when
        CustomException exception = assertThrows(CustomException.class,
            () ->  friendService.refuseFriendship(createMockAuthentication(user), notificationId, requester));

        // then
        assertEquals(NOT_FOUND_USER, exception.getErrorCode());
    }

    private Authentication createMockAuthentication(User user) {
        return new UsernamePasswordAuthenticationToken(new PrincipalDetails(user), null);
    }
    @Test
    @DisplayName("친구 요청 거절 - 실패_이미 친구 관계")
    public void testRefuseFriendship_Fail_ALREADY_REGISTERED_FRIENDSHIP() {
        // given
        Long notificationId = 1L;
        User user = User.builder()
            .id(1L)
            .name("user")
            .email("user1@example.com")
            .build();
        User requester = User.builder()
            .id(1L)
            .name("requester")
            .email("requester@example.com")
            .build();

        when(userRepository.findByName(requester.getName())).thenReturn(Optional.of(requester));
        Friendship userFriendship = Friendship.builder()
            .user(requester)
            .friend(user)
            .friendshipStatus(FriendshipStatus.ACCEPTED)
            .build();
        Friendship requesterFriendship = Friendship.builder()
            .user(requester)
            .friend(user)
            .friendshipStatus(FriendshipStatus.ACCEPTED)
            .build();
        when(friendshipRepository.findByUserAndFriend(user, requester)).thenReturn(Optional.of(userFriendship));
        when(friendshipRepository.findByUserAndFriend(requester,user)).thenReturn(Optional.of(requesterFriendship));
        // when
        CustomException exception = assertThrows(CustomException.class,
            () ->  friendService.refuseFriendship(createMockAuthentication(user), notificationId, requester.getName()));

        // then
        assertEquals(ALREADY_REGISTERED_FRIEND, exception.getErrorCode());
    }

    // 유저 차단
    @Test
    @DisplayName("유저 차단 - 성공")
    public void testBlock() {
        // given
        User user = User.builder()
            .id(1L)
            .name("user")
            .email("user1@example.com")
            .build();
        User friend = User.builder()
            .id(1L)
            .name("friend")
            .email("requester@example.com")
            .build();

        when(userRepository.findByName(friend.getName())).thenReturn(Optional.of(friend));
        when(friendshipRepository.findByUserAndFriend(user,friend)).thenReturn(Optional.empty());
        // when
        String result = friendService.block(createMockAuthentication(user), friend.getName());

        // then
        assertEquals("friend 님을 차단했습니다.", result);
    }
    @Test
    @DisplayName("유저 차단 - 실패_유저 찾을 수 없음")
    public void testBlock_Fail_NOT_FOUND_USER() {
        // given
        String friend = "friend";
        User user = User.builder()
            .id(1L)
            .name("user")
            .email("user1@example.com")
            .build();

        when(userRepository.findByName(friend)).thenReturn(Optional.empty());
        // when
        CustomException exception = assertThrows(CustomException.class,
            () ->  friendService.block(createMockAuthentication(user), friend));

        // then
        assertEquals(NOT_FOUND_USER, exception.getErrorCode());
    }

    // 유저 차단 해제
    @Test
    @DisplayName("유저 차단 해제- 성공")
    public void testUnBlock() {
        // given
        User user = User.builder()
            .id(1L)
            .name("user")
            .email("user1@example.com")
            .build();
        User friend = User.builder()
            .id(1L)
            .name("friend")
            .email("requester@example.com")
            .build();
        Friendship userFriendship = Friendship.builder()
            .user(user)
            .friend(friend)
            .friendshipStatus(FriendshipStatus.BLOCKED)
            .build();

        when(userRepository.findByName(friend.getName())).thenReturn(Optional.of(friend));
        when(friendshipRepository.findByUserAndFriend(user,friend)).thenReturn(Optional.of(userFriendship));
        // when
        String result = friendService.unBlock(createMockAuthentication(user), friend.getName());

        // then
        assertEquals("friend 님을 차단 해제 했습니다.", result);
    }
    @Test
    @DisplayName("유저 차단 해제- 실패_유저 찾을 수 없음")
    public void testUnBlock_Fail_NOT_FOUND_USER() {
        // given
        String friend = "friend";
        User user = User.builder()
            .id(1L)
            .name("user")
            .email("user1@example.com")
            .build();

        when(userRepository.findByName(anyString())).thenReturn(Optional.empty());
        // when
        CustomException exception = assertThrows(CustomException.class,
            () ->  friendService.unBlock(createMockAuthentication(user), friend));

        // then
        assertEquals(NOT_FOUND_USER, exception.getErrorCode());
    }
    @Test
    @DisplayName("유저 차단 해제- 실패_차단된 유저가 아님")
    public void testUnBlock_Fail_FRIENDSHIP_STATUS_IS_NOT_BLOCKED() {
        // given
        User user = User.builder()
            .id(1L)
            .name("user")
            .email("user1@example.com")
            .build();
        User friend = User.builder()
            .id(1L)
            .name("friend")
            .email("requester@example.com")
            .build();
        Friendship userFriendship = Friendship.builder()
            .user(user)
            .friend(friend)
            .friendshipStatus(FriendshipStatus.ACCEPTED)
            .build();

        when(userRepository.findByName(friend.getName())).thenReturn(Optional.of(friend));
        when(friendshipRepository.findByUserAndFriend(user,friend)).thenReturn(Optional.of(userFriendship));
        // when
        CustomException exception = assertThrows(CustomException.class,
            () ->  friendService.unBlock(createMockAuthentication(user), friend.getName()));

        // then
        assertEquals(FRIENDSHIP_STATUS_IS_NOT_BLOCKED, exception.getErrorCode());
    }
    @Test
    @DisplayName("유저 차단 해제- 실패_관련 없는 유저")
    public void testUnBlock_Fail_FRIENDSHIP_STATUS_IS_NOT_BLOCKED2() {
        // given
        User user = User.builder()
            .id(1L)
            .name("user")
            .email("user1@example.com")
            .build();
        User friend = User.builder()
            .id(1L)
            .name("friend")
            .email("requester@example.com")
            .build();

        when(userRepository.findByName(friend.getName())).thenReturn(Optional.of(friend));
        when(friendshipRepository.findByUserAndFriend(user,friend)).thenReturn(Optional.empty());
        // when
        CustomException exception = assertThrows(CustomException.class,
            () ->  friendService.unBlock(createMockAuthentication(user), friend.getName()));

        // then
        assertEquals(FRIENDSHIP_STATUS_IS_NOT_BLOCKED, exception.getErrorCode());
    }

    // 친구 관계 상태에 대한 친구 목록 조회
    @Test
    @DisplayName("친구 관계 상태에 대한 친구 목록 조회 - 친구사이")
    void testGetFriends_Accept() {
        //given
        User user = User.builder()
            .id(1L)
            .name("user")
            .email("user1@example.com")
            .build();
        User friend1 = User.builder()
            .id(1L)
            .name("friend")
            .email("requester@example.com")
            .nationality(Nationality.UK)
            .language(Language.FR)
            .randomApproval(true)
            .build();

        List<Friendship> userFriends= new ArrayList<>();
        Friendship userFriendship1 = Friendship.builder()
            .user(user)
            .friend(friend1)
            .friendshipStatus(FriendshipStatus.ACCEPTED)
            .build();
        userFriends.add(userFriendship1);

        when(friendshipRepository.findByUserAndFriendshipStatus(user, FriendshipStatus.ACCEPTED)).thenReturn(userFriends);
        //when
        Set<FriendInfoDto> result = friendService.getFriends(createMockAuthentication(user), FriendshipStatus.ACCEPTED);
        //then
        assertEquals(1, result.size());
    }
}