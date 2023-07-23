package com.example.translationchat.client.controller;

import com.example.translationchat.client.domain.form.SignUpForm;
import com.example.translationchat.client.service.UserService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<String> userSignUp(@Valid @RequestBody SignUpForm form) {
        return ResponseEntity.ok(userService.signUp(form));
    }

}
