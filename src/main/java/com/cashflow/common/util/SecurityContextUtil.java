package com.cashflow.common.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class SecurityContextUtil {

    private SecurityContextUtil() {
    }

    public static Optional<Authentication> getAuthentication() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());
    }

    public static Optional<String> getCurrentUserEmail() {
        return getAuthentication()
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getName);
    }

    public static Optional<String> getCurrentToken() {
        return getAuthentication()
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getCredentials)
                .map(Object::toString);
    }

    public static List<String> getCurrentUserAuthorities() {
        return getAuthentication()
                .filter(Authentication::isAuthenticated)
                .map(auth -> auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList())
                .orElse(Collections.emptyList());
    }

    public static boolean isAuthenticated() {
        return getAuthentication()
                .map(Authentication::isAuthenticated)
                .orElse(false);
    }
}
