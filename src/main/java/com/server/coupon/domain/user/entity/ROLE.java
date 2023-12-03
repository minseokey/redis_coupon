package com.server.coupon.domain.user.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ROLE {
    ADMIN("ROLE_ADMIN"),
    USER("ROLE_USER");
    private final String key;
}
