package com.example.translationchat.client.controller;

import com.example.translationchat.client.domain.dto.FriendInfoDto;
import com.example.translationchat.client.service.FriendService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/friend")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @GetMapping
    public ResponseEntity<List<FriendInfoDto>> search(@RequestParam String name) {
        return ResponseEntity.ok(friendService.searchByUserName(name));
    }
}
