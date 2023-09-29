package com.example.translationchat.domain.user.repository;

import com.example.translationchat.domain.user.entity.QUser;
import com.example.translationchat.domain.user.entity.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<User> searchByName(String name) {
        QUser user = QUser.user;
        return queryFactory.selectFrom(user)
            .where(user.name.contains(name))
            .fetch();
    }
}
