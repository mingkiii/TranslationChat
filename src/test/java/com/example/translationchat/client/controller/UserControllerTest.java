package com.example.translationchat.client.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.translationchat.client.domain.dto.MyInfoDto;
import com.example.translationchat.client.domain.form.LoginForm;
import com.example.translationchat.client.domain.form.SignUpForm;
import com.example.translationchat.client.domain.form.UpdateUserForm;
import com.example.translationchat.client.domain.model.User;
import com.example.translationchat.client.domain.type.ActiveStatus;
import com.example.translationchat.client.domain.type.Language;
import com.example.translationchat.client.domain.type.Nationality;
import com.example.translationchat.client.service.UserService;
import com.example.translationchat.common.security.principal.PrincipalDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("회원 가입 - 성공")
    void testSignUp_Success() throws Exception {
        SignUpForm form = SignUpForm.builder()
            .email("test@test.com")
            .name("test")
            .password("test123!")
            .nationality(Nationality.UK)
            .language(Language.FR)
            .build();

        String expectedResult = "회원가입이 완료되었습니다.";
        when(userService.signUp(any(SignUpForm.class))).thenReturn(expectedResult);

        mockMvc.perform(post("/user/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(form)))
            .andExpect(status().isOk())
            .andExpect(content().string(expectedResult));

        verify(userService, times(1)).signUp(any(SignUpForm.class));
    }

    // 로그인 - 성공
    @Test
    @DisplayName("로그인 - 성공")
    void testLogin_Success() throws Exception {
        LoginForm form = LoginForm.builder()
            .email("test@test.com")
            .password("test123!")
            .build();

        when(userService.login(any(LoginForm.class))).thenReturn("로그인 성공 토큰");

        mockMvc.perform(post("/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(form)))
            .andExpect(status().isOk())
            .andExpect(content().string("로그인 성공 토큰"));
    }

    // 회원 탈퇴
    @Test
    @DisplayName("회원 탈퇴 - 성공")
    @WithMockUser(username = "test@test.com")
    void testWithdraw_Success() throws Exception {
        User user = User.builder()
            .email("test@test.com")
            .password("test123!")
            .status(ActiveStatus.ONLINE)
            .build();

        // 가짜 사용자로 인증 정보 생성
        PrincipalDetails principalDetails = new PrincipalDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principalDetails, null,
            principalDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        mockMvc.perform(delete("/user")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    // 회원(본인) 정보 조회 - 성공
    @Test
    @DisplayName("회원(본인) 정보 조회 - 성공")
    @WithMockUser(username = "test@test.com")
    void testGetInfo_Success() throws Exception {
        User user = User.builder()
            .email("test@test.com")
            .name("test")
            .nationality(Nationality.UK)
            .language(Language.FR)
            .build();
        MyInfoDto userInfoDto = MyInfoDto.from(user);

        when(userService.getInfo(any(Authentication.class))).thenReturn(userInfoDto);

        mockMvc.perform(get("/user")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email", is("test@test.com")))
            .andExpect(jsonPath("$.name", is("test")))
            .andExpect(jsonPath("$.nationality", is("UK")))
            .andExpect(jsonPath("$.language", is("French")));
    }

    // 회원(본인) 정보 수정 - 성공
    @Test
    @DisplayName("회원(본인) 정보 수정 - 성공")
    @WithMockUser(username = "test@test.com")
    void testUpdateInfo_Success() throws Exception {
        UpdateUserForm form = UpdateUserForm.builder()
            .name("new_test")
            .password("new1234!")
            .nationality(Nationality.UK)
            .language(Language.FR)
            .build();

        MyInfoDto updatedUserInfo = MyInfoDto.builder()
            .email("test@test.com")
            .name("new_test")
            .nationality(Nationality.UK)
            .language(Language.FR.getDisplayName())
            .build();

        when(userService.updateInfo(any(Authentication.class), any(
            UpdateUserForm.class))).thenReturn(updatedUserInfo);

        mockMvc.perform(put("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(form)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email", is("test@test.com")))
            .andExpect(jsonPath("$.name", is("new_test")))
            .andExpect(jsonPath("$.nationality", is("UK")))
            .andExpect(jsonPath("$.language", is("French")));
    }
}