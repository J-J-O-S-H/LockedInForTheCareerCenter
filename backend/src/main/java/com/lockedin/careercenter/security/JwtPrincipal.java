package com.lockedin.careercenter.security;

import java.util.Collection;
import java.util.List;

import com.lockedin.careercenter.model.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public record JwtPrincipal(
        String userId,
        String email,
        UserRole role) {

    public Collection<? extends GrantedAuthority> authorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
}
