package com.example.translationchat.common.security.principal;

import com.example.translationchat.domain.user.entity.User;
import java.util.Collection;
import java.util.Collections;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@RequiredArgsConstructor
public class PrincipalDetails implements UserDetails {

    private final String email;
    private final String password;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public static PrincipalDetails create(User user) {
        return new PrincipalDetails(user.getEmail(), user.getPassword());
    }
}
