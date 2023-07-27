package com.example.translationchat.client.service;

import static com.example.translationchat.common.exception.ErrorCode.NOT_FOUND_USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.translationchat.client.domain.dto.FriendInfoDto;
import com.example.translationchat.client.domain.model.User;
import com.example.translationchat.client.domain.repository.UserRepository;
import com.example.translationchat.client.domain.type.Language;
import com.example.translationchat.client.domain.type.Nationality;
import com.example.translationchat.common.exception.CustomException;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FriendServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FriendService friendService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        friendService = new FriendService(userRepository);
    }

    @Test
    @DisplayName("친구 찾기 - 성공")
    void searchByUserName_Success() {
        //given
        String name = "min";
        User user1 = User.builder()
            .name("kim_minHo")
            .nationality(Nationality.UK)
            .language(Language.FR)
            .build();
        User user2 = User.builder()
            .name("park_minSu")
            .nationality(Nationality.KOREA)
            .language(Language.KO)
            .build();
        List<User> users = Arrays.asList(user1, user2);

        when(userRepository.searchByName(name)).thenReturn(users);
        //when
        List<FriendInfoDto> result = friendService.searchByUserName(name);
        //then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("kim_minHo");
        assertThat(result.get(1).getName()).isEqualTo("park_minSu");

        verify(userRepository, times(1)).searchByName(name);
    }

    @Test
    void testSearchByUserName_Fail_UsersNotFound() {
        // given
        String name = "John";
        when(userRepository.searchByName(name)).thenReturn(List.of());
        // when
        CustomException exception = assertThrows(CustomException.class,
            () -> friendService.searchByUserName(name));
        // then
        assertEquals(NOT_FOUND_USER, exception.getErrorCode());
        verify(userRepository, times(1)).searchByName(name);
    }

}