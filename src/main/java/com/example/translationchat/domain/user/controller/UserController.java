package com.example.translationchat.domain.user.controller;

import com.example.translationchat.domain.user.dto.MyInfoDto;
import com.example.translationchat.domain.user.dto.UserInfoDto;
import com.example.translationchat.common.security.principal.PrincipalDetails;
import com.example.translationchat.domain.user.entity.User;
import com.example.translationchat.domain.user.form.LoginForm;
import com.example.translationchat.domain.user.form.SignUpForm;
import com.example.translationchat.domain.user.form.UpdatePasswordForm;
import com.example.translationchat.domain.user.form.UpdateUserForm;
import com.example.translationchat.domain.user.service.UserService;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원 가입
    @PostMapping("/auth/signup")
    public ResponseEntity<String> signUp(@Valid @RequestBody SignUpForm form) {
        return ResponseEntity.ok(userService.signUp(form));
    }

    // 로그인
    @PostMapping("/auth/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginForm form) {
        return ResponseEntity.ok(userService.login(form));
    }

    // 로그아웃
    @PutMapping("/logout")
    public void logout(@AuthenticationPrincipal PrincipalDetails principal) {
        User user = userService.getUserByEmail(principal.getEmail());
        userService.logout(user);
    }

    // 회원 탈퇴
    @DeleteMapping
    public void withdraw(@AuthenticationPrincipal PrincipalDetails principal) {
        User user = userService.getUserByEmail(principal.getEmail());
        userService.delete(user);
    }

    // 회원(본인) 정보 조회
    @GetMapping("/my-info")
    public ResponseEntity<MyInfoDto> getInfo(@AuthenticationPrincipal PrincipalDetails principal) {
        User user = userService.getUserByEmail(principal.getEmail());
        return ResponseEntity.ok(MyInfoDto.from(user));
    }

    // 회원(본인) 정보 수정
    @PutMapping("/update")
    public ResponseEntity<MyInfoDto> updateInfo(
        @AuthenticationPrincipal PrincipalDetails principal,
        @RequestBody @Valid UpdateUserForm form
    ) {
        User user = userService.getUserByEmail(principal.getEmail());
        User updateUser = userService.updateInfo(user, form);
        return ResponseEntity.ok(MyInfoDto.from(updateUser));
    }

    // 비밀번호 변경
    @PutMapping("/password")
    public void updatePassword(
        @AuthenticationPrincipal PrincipalDetails principal,
        @RequestBody @Valid UpdatePasswordForm form
    ) {
        User user = userService.getUserByEmail(principal.getEmail());
        userService.updatePassword(user, form);
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserInfoDto>> search(@RequestParam String name) {
        return ResponseEntity.ok(userService.searchByUserName(name).stream()
            .map(UserInfoDto::from)
            .collect(Collectors.toList())
        );
    }
}
