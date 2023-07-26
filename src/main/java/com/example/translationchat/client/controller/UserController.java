package com.example.translationchat.client.controller;

import com.example.translationchat.client.domain.dto.UserInfoDto;
import com.example.translationchat.client.domain.form.LoginForm;
import com.example.translationchat.client.domain.form.SignUpForm;
import com.example.translationchat.client.domain.form.UpdateUserForm;
import com.example.translationchat.client.service.UserService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원 가입
    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@Valid @RequestBody SignUpForm form) {
        return ResponseEntity.ok(userService.signUp(form));
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginForm form) {
        return ResponseEntity.ok(userService.login(form));
    }

    // 회원 탈퇴
    @DeleteMapping
    public void withdraw(Authentication authentication) {
        userService.delete(authentication);
    }

    // 회원(본인) 정보 조회
    @GetMapping
    public ResponseEntity<UserInfoDto> getInfo(Authentication authentication) {
        return ResponseEntity.ok(userService.getInfo(authentication));
    }

    // 회원(본인) 정보 수정
    @PutMapping
    public ResponseEntity<UserInfoDto> updateInfo(
        Authentication authentication, @Valid UpdateUserForm form
    ) {
        return ResponseEntity.ok(userService.updateInfo(authentication, form));
    }

}
