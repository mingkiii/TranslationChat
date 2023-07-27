package com.example.translationchat.client.controller;

import static com.example.translationchat.common.exception.ErrorCode.NOT_FOUND_USER;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.translationchat.client.domain.dto.FriendInfoDto;
import com.example.translationchat.client.service.FriendService;
import com.example.translationchat.common.exception.CustomException;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@SpringBootTest
class FriendControllerTest {
    @Mock
    private FriendService friendService;

    @InjectMocks
    private FriendController friendController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(friendController).build();
    }

    @Test
    @DisplayName("친구 찾기 - 성공")
    public void testSearchByUserName() throws Exception {
        // given
        String name = "John";
        FriendInfoDto friend1 = FriendInfoDto.builder().name("minJu").build();
        FriendInfoDto friend2 = FriendInfoDto.builder().name("kim_min").build();
        List<FriendInfoDto> friendList = Arrays.asList(friend1, friend2);

        when(friendService.searchByUserName(name)).thenReturn(friendList);
        // when, then
        mockMvc.perform(get("/friend")
                .param("name", name)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].name", is("minJu")))
            .andExpect(jsonPath("$[1].name", is("kim_min")));

        verify(friendService, times(1)).searchByUserName(name);
    }
    @Test
    @DisplayName("친구 찾기 - 실패")
    public void testSearchByUserName_fail() {
        // given
        String name = "John";

        when(friendService.searchByUserName(name))
            .thenThrow(new CustomException(NOT_FOUND_USER));
        // when
        CustomException exception = assertThrows(CustomException.class,
            () -> friendController.search(name));
        // then
        assertEquals(NOT_FOUND_USER, exception.getErrorCode());
        verify(friendService, times(1)).searchByUserName(name);
    }
}