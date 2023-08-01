package com.example.translationchat.client.service;

import static com.example.translationchat.common.exception.ErrorCode.ALREADY_REGISTERED_FAVORITE;
import static com.example.translationchat.common.exception.ErrorCode.CAN_NOT_FAVORITE_YOURSELF;
import static com.example.translationchat.common.exception.ErrorCode.NOT_FOUND_USER;
import static com.example.translationchat.common.exception.ErrorCode.USER_IS_BLOCKED;
import static com.example.translationchat.common.exception.ErrorCode.USER_IS_NOT_BLOCKED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.example.translationchat.client.domain.dto.UserInfoDto;
import com.example.translationchat.client.domain.model.Favorite;
import com.example.translationchat.client.domain.model.User;
import com.example.translationchat.client.domain.repository.FavoriteRepository;
import com.example.translationchat.client.domain.repository.UserRepository;
import com.example.translationchat.client.domain.type.Language;
import com.example.translationchat.client.domain.type.Nationality;
import com.example.translationchat.common.exception.CustomException;
import com.example.translationchat.common.security.principal.PrincipalDetails;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@SpringBootTest
class FavoriteServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FavoriteRepository friendshipRepository;

    @InjectMocks
    private FavoriteService friendService;

    // 즐겨찾기 등록
    @Test
    @DisplayName("즐겨찾기 등록 - 성공")
    public void testRegister() {
        // given
        User user = User.builder()
            .id(1L)
            .name("user")
            .email("user1@example.com")
            .build();
        User favorite = User.builder()
            .id(1L)
            .name("favorite")
            .email("favorite@example.com")
            .build();

        when(userRepository.findByName(anyString())).thenReturn(Optional.of(favorite));
        when(friendshipRepository.findByUserAndFavorite(user, favorite))
            .thenReturn(Optional.empty());
        // when
        String result = friendService.register(
            createMockAuthentication(user), favorite.getName());

        // then
        assertEquals("favorite 님을 즐겨찾기에 추가했습니다.", result);
    }
    @Test
    @DisplayName("즐겨찾기 등록 - 실패_유저 자신에게 즐겨찾기")
    public void testRegister_Fail_CAN_NOT_FAVORITE_YOURSELF() {
        // given
        String favoriteName = "user";
        User user = User.builder()
            .id(1L)
            .name("user")
            .email("user1@example.com")
            .build();

        // when
        CustomException exception = assertThrows(CustomException.class,
            () -> friendService.register(createMockAuthentication(user), favoriteName));

        // then
        assertEquals(CAN_NOT_FAVORITE_YOURSELF, exception.getErrorCode());
    }
    @Test
    @DisplayName("즐겨찾기 등록- 실패_상대방 찾을 수 없음")
    public void testRegister_Fail_NOT_FOUND_USER() {
        // given
        String favoriteName = "favorite";
        User user = User.builder()
            .id(1L)
            .name("user")
            .email("user1@example.com")
            .build();

        when(userRepository.findByName(anyString())).thenReturn(Optional.empty());
        // when
        CustomException exception = assertThrows(CustomException.class,
            () -> friendService.register(createMockAuthentication(user), favoriteName));

        // then
        assertEquals(NOT_FOUND_USER, exception.getErrorCode());
    }
    @Test
    @DisplayName("즐겨찾기 등록 - 실패_이미 즐겨찾기 등록")
    public void testRegister_Fail_ALREADY_REGISTERED_FAVORITE() {
        // given
        User user = User.builder()
            .id(1L)
            .name("user")
            .email("user1@example.com")
            .build();
        User favorite = User.builder()
            .id(1L)
            .name("favorite")
            .email("friend@example.com")
            .build();
        Favorite userFavorite = Favorite.builder()
            .user(user)
            .favorite(favorite)
            .blocked(false)
            .build();

        when(userRepository.findByName(anyString())).thenReturn(Optional.of(favorite));
        when(friendshipRepository.findByUserAndFavorite(
            any(User.class), any(User.class))).thenReturn(Optional.of(userFavorite));
        // when
        CustomException exception = assertThrows(CustomException.class,
            () -> friendService.register(createMockAuthentication(user), favorite.getName()));

        // then
        assertEquals(ALREADY_REGISTERED_FAVORITE, exception.getErrorCode());
    }
    @Test
    @DisplayName("즐겨찾기 등록 - 실패_유저가 차단한 친구")
    public void testRegister_Fail_USER_IS_BLOCKED() {
        // given
        User user = User.builder()
            .id(1L)
            .name("user")
            .email("user1@example.com")
            .build();
        User favorite = User.builder()
            .id(1L)
            .name("favorite")
            .email("friend@example.com")
            .build();
        Favorite userFavorite = Favorite.builder()
            .user(user)
            .favorite(favorite)
            .blocked(true)
            .build();

        when(userRepository.findByName(anyString())).thenReturn(Optional.of(favorite));
        when(friendshipRepository.findByUserAndFavorite(
            any(User.class), any(User.class))).thenReturn(Optional.of(userFavorite));
        // when
        CustomException exception = assertThrows(CustomException.class,
            () -> friendService.register(createMockAuthentication(user), favorite.getName()));

        // then
        assertEquals(USER_IS_BLOCKED, exception.getErrorCode());
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
        User favorite = User.builder()
            .id(1L)
            .name("favorite")
            .email("friend@example.com")
            .build();

        when(userRepository.findByName(anyString())).thenReturn(Optional.of(favorite));
        when(friendshipRepository.findByUserAndFavorite(user,favorite)).thenReturn(Optional.empty());
        // when
        String result = friendService.block(createMockAuthentication(user), favorite.getName());

        // then
        assertEquals("favorite 님을 차단했습니다.", result);
    }
    @Test
    @DisplayName("유저 차단 - 실패_유저 찾을 수 없음")
    public void testBlock_Fail_NOT_FOUND_USER() {
        // given
        String favoriteName = "favorite";
        User user = User.builder()
            .id(1L)
            .name("user")
            .email("user1@example.com")
            .build();

        when(userRepository.findByName(anyString())).thenReturn(Optional.empty());
        // when
        CustomException exception = assertThrows(CustomException.class,
            () -> friendService.block(createMockAuthentication(user), favoriteName));

        // then
        assertEquals(NOT_FOUND_USER, exception.getErrorCode());
    }
    @Test
    @DisplayName("유저 차단 - 실패_이미 차단한 친구")
    public void testBlock_Fail_USER_IS_BLOCKED() {
        // given
        User user = User.builder()
            .id(1L)
            .name("user")
            .email("user1@example.com")
            .build();
        User favorite = User.builder()
            .id(1L)
            .name("favorite")
            .email("friend@example.com")
            .build();
        Favorite userFavorite = Favorite.builder()
            .user(user)
            .favorite(favorite)
            .blocked(true)
            .build();

        when(userRepository.findByName(anyString())).thenReturn(Optional.of(favorite));
        when(friendshipRepository.findByUserAndFavorite(
            any(User.class), any(User.class))).thenReturn(Optional.of(userFavorite));
        // when
        CustomException exception = assertThrows(CustomException.class,
            () -> friendService.block(createMockAuthentication(user), favorite.getName()));

        // then
        assertEquals(USER_IS_BLOCKED, exception.getErrorCode());
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
        User favorite = User.builder()
            .id(1L)
            .name("favorite")
            .email("friend@example.com")
            .build();
        Favorite userFavorite = Favorite.builder()
            .user(user)
            .favorite(favorite)
            .blocked(true)
            .build();

        when(userRepository.findByName(anyString())).thenReturn(Optional.of(favorite));
        when(friendshipRepository.findByUserAndFavorite(user,favorite))
            .thenReturn(Optional.of(userFavorite));
        // when
        String result = friendService.unBlock(createMockAuthentication(user), favorite.getName());

        // then
        assertEquals("favorite 님을 차단 해제 했습니다.", result);
    }
    @Test
    @DisplayName("유저 차단 해제- 실패_유저 찾을 수 없음")
    public void testUnBlock_Fail_NOT_FOUND_USER() {
        // given
        String favoriteName = "favorite";
        User user = User.builder()
            .id(1L)
            .name("user")
            .email("user1@example.com")
            .build();

        when(userRepository.findByName(anyString())).thenReturn(Optional.empty());
        // when
        CustomException exception = assertThrows(CustomException.class,
            () -> friendService.unBlock(createMockAuthentication(user), favoriteName));

        // then
        assertEquals(NOT_FOUND_USER, exception.getErrorCode());
    }
    @Test
    @DisplayName("유저 차단 해제- 실패_차단된 유저가 아님")
    public void testUnBlock_Fail_USER_IS_NOT_BLOCKED() {
        // given
        User user = User.builder()
            .id(1L)
            .name("user")
            .email("user1@example.com")
            .build();
        User favorite = User.builder()
            .id(1L)
            .name("favorite")
            .email("friend@example.com")
            .build();
        Favorite userFavorite = Favorite.builder()
            .user(user)
            .favorite(favorite)
            .blocked(false)
            .build();

        when(userRepository.findByName(anyString())).thenReturn(Optional.of(favorite));
        when(friendshipRepository.findByUserAndFavorite(user,favorite))
            .thenReturn(Optional.of(userFavorite));
        // when
        CustomException exception = assertThrows(CustomException.class,
            () ->  friendService.unBlock(createMockAuthentication(user), favorite.getName()));

        // then
        assertEquals(USER_IS_NOT_BLOCKED, exception.getErrorCode());
    }
    @Test
    @DisplayName("유저 차단 해제- 실패_관련 없는 유저")
    public void testUnBlock_Fail_USER_IS_NOT_BLOCKED2() {
        // given
        User user = User.builder()
            .id(1L)
            .name("user")
            .email("user1@example.com")
            .build();
        User favorite = User.builder()
            .id(1L)
            .name("favorite")
            .email("friend@example.com")
            .build();

        when(userRepository.findByName(anyString())).thenReturn(Optional.of(favorite));
        when(friendshipRepository.findByUserAndFavorite(user,favorite))
            .thenReturn(Optional.empty());
        // when
        CustomException exception = assertThrows(CustomException.class,
            () ->  friendService.unBlock(createMockAuthentication(user), favorite.getName()));

        // then
        assertEquals(USER_IS_NOT_BLOCKED, exception.getErrorCode());
    }

    // 즐겨찾기 목록 조회
    @Test
    @DisplayName("즐겨찾기 목록 조회")
    void testFavoriteList() {
        //given
        User user = User.builder()
            .id(1L)
            .name("user")
            .email("user1@example.com")
            .build();
        User favorite1 = User.builder()
            .id(1L)
            .name("favorite1")
            .email("favorite1@example.com")
            .nationality(Nationality.UK)
            .language(Language.FR)
            .randomApproval(true)
            .build();
        User favorite2 = User.builder()
            .id(1L)
            .name("favorite2")
            .email("favorite2@example.com")
            .nationality(Nationality.UK)
            .language(Language.FR)
            .randomApproval(true)
            .build();

        List<Favorite> favoriteList= new ArrayList<>();
        Favorite userFavorite1 = Favorite.builder()
            .user(user)
            .favorite(favorite1)
            .blocked(false)
            .build();
        Favorite userFavorite2 = Favorite.builder()
            .user(user)
            .favorite(favorite2)
            .blocked(false)
            .build();
        favoriteList.add(userFavorite1);
        favoriteList.add(userFavorite2);

        when(friendshipRepository.findByUserAndBlocked(user, false)).thenReturn(favoriteList);
        //when
        List<UserInfoDto> result = friendService.favoriteList(createMockAuthentication(user));
        //then
        assertEquals(2, result.size());
    }
    // 차단 유저 목록 조회
    @Test
    @DisplayName("차단 유저 목록 조회")
    void testBlockedList() {
        //given
        User user = User.builder()
            .id(1L)
            .name("user")
            .email("user1@example.com")
            .build();
        User favorite1 = User.builder()
            .id(2L)
            .name("favorite1")
            .email("favorite1@example.com")
            .nationality(Nationality.UK)
            .language(Language.FR)
            .randomApproval(true)
            .build();
        User favorite2 = User.builder()
            .id(3L)
            .name("favorite2")
            .email("favorite2@example.com")
            .nationality(Nationality.UK)
            .language(Language.FR)
            .randomApproval(true)
            .build();

        List<Favorite> blockedList= new ArrayList<>();
        Favorite userFavorite1 = Favorite.builder()
            .user(user)
            .favorite(favorite1)
            .blocked(true)
            .build();
        Favorite userFavorite2 = Favorite.builder()
            .user(user)
            .favorite(favorite2)
            .blocked(true)
            .build();
        blockedList.add(userFavorite1);
        blockedList.add(userFavorite2);

        when(friendshipRepository.findByUserAndBlocked(user, true)).thenReturn(blockedList);
        //when
        List<UserInfoDto> result = friendService.blockedList(createMockAuthentication(user));
        //then
        assertEquals(2, result.size());
    }
    private Authentication createMockAuthentication(User user) {
        return new UsernamePasswordAuthenticationToken(new PrincipalDetails(user), null);
    }
}