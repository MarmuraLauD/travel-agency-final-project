package com.epam.finaltask.model;

import lombok.Getter;

@Getter
public enum Permission {
    USER_READ("user:read"),
    USER_UPDATE("user:update"),
    USER_CREATE("user:create"),
    USER_DELETE("user:delete"),
    VOUCHER_READ("voucher:read"),
    VOUCHER_UPDATE("voucher:update"),
    VOUCHER_CREATE("voucher:create"),
    VOUCHER_DELETE("voucher:delete"),;

    public final String permission;

    Permission(String permission) {
        this.permission = permission;
    }
}
