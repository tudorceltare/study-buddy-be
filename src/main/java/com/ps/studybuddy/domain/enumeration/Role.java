package com.ps.studybuddy.domain.enumeration;

import lombok.Getter;

import static com.ps.studybuddy.security.constant.Authority.*;

@Getter
public enum Role {
    ROLE_USER(USER_AUTHORITIES),
//    ROLE_HR(HR_AUTHORITIES),
//    ROLE_MANAGER(MANAGER_AUTHORITIES),
//    ROLE_ADMIN(ADMIN_AUTHORITIES),
    ROLE_SUPER_ADMIN(SUPER_ADMIN_AUTHORITIES);

    private String[] authorities;

    Role(String... authorities) {
        this.authorities = authorities;
    }
}
