package com.epam.finaltask.model;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public enum Role {
    ADMIN(Set.of(Permission.ADMIN_READ, Permission.ADMIN_DELETE, Permission.ADMIN_UPDATE, Permission.ADMIN_WRITE)),
    MANAGER(Set.of(Permission.MANAGER_UPDATE)),
    USER(Set.of(Permission.USER_READ, Permission.USER_DELETE, Permission.USER_UPDATE, Permission.USER_CREATE));

    private final Set<Permission> permissions;

    Role(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public List<SimpleGrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        permissions.forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission.getPermission())));
        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
        return authorities;
    }
}
