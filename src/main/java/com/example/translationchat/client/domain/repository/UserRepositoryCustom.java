package com.example.translationchat.client.domain.repository;

import com.example.translationchat.client.domain.model.User;
import java.util.List;

public interface UserRepositoryCustom {
    List<User> searchByName(String name);
}
