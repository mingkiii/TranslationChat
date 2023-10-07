package com.example.translationchat.domain.user.repository;

import com.example.translationchat.domain.user.entity.User;
import java.util.List;

public interface UserRepositoryCustom {
    List<User> searchByName(String name);
}
