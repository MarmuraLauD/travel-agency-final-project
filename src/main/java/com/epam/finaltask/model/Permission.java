package com.epam.finaltask.model;

import lombok.Getter;

@Getter
public enum Permission {
	ADMIN_READ("admin:read"),
    ADMIN_UPDATE("admin:update"),
    ADMIN_WRITE("admin:write"),
    ADMIN_DELETE("admin:delete"),
    MANAGER_UPDATE("manager:update"),
    USER_READ("user:read"),
    USER_UPDATE("user:update"),
    USER_CREATE("user:create"),
    USER_DELETE("user:delete"),;

    public final String permission;

    Permission(String permission) {
        this.permission = permission;
    }
}
