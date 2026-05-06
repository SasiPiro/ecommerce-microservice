package com.ecommerce.user.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;

public class SecurityUtil {

    public static  boolean hasPermission(String permission) {
        // RBAC + ABAC
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
        return authorities.stream().anyMatch(a -> a.getAuthority().equalsIgnoreCase(permission));
    }

    public static Long getUserId() {
        // RBAC + ABAC
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong((String) auth.getDetails());
        return userId;
    }
}
