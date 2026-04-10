package com.epam.finaltask.model;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public enum Role {
    ADMIN(Set.of(Permission.USER_READ, Permission.USER_CREATE, Permission.USER_UPDATE, Permission.USER_DELETE,
            Permission.VOUCHER_CREATE, Permission.VOUCHER_UPDATE, Permission.VOUCHER_DELETE, Permission.VOUCHER_READ)),
    MANAGER(Set.of(Permission.USER_UPDATE, Permission.VOUCHER_UPDATE,  Permission.VOUCHER_READ,  Permission.USER_READ)),
    USER(Set.of(Permission.VOUCHER_READ)),;

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
