package com.example.translationchat.client.service;

import static com.example.translationchat.common.exception.ErrorCode.NOT_FOUND_USER;

import com.example.translationchat.client.domain.dto.FriendInfoDto;
import com.example.translationchat.client.domain.model.User;
import com.example.translationchat.client.domain.repository.UserRepository;
import com.example.translationchat.common.exception.CustomException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final UserRepository userRepository;

    public List<FriendInfoDto> searchByUserName(String name) {
        List<User> users = userRepository.searchByName(name);
        if (users.isEmpty()) {
            throw new CustomException(NOT_FOUND_USER);
        }
        return users.stream()
            .map(FriendInfoDto::from)
            .collect(Collectors.toList());
    }
}
