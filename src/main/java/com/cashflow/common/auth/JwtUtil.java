package com.cashflow.common.auth;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.cashflow.common.constants.JwtClaimConstants;
import com.cashflow.common.exception.UnauthorizedException;

import io.jsonwebtoken.Claims;

@Component
public class JwtUtil {

    private final JwtTokenValidator jwtTokenValidator;

    public JwtUtil(JwtTokenValidator jwtTokenValidator) {
        this.jwtTokenValidator = jwtTokenValidator;
    }

    public Claims getClaims(String token) {
        return jwtTokenValidator.validateAndExtractClaims(token);
    }

    public Long extractUserId(String token) {
        Claims claims = getClaims(token);
        Object userIdObj = claims.get(JwtClaimConstants.USER_ID);
        if (userIdObj == null) {
            userIdObj = claims.get("user_id");
        }
        if (userIdObj == null) {
            return null;
        }
        if (userIdObj instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.valueOf(userIdObj.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public String extractEmail(String token) {
        Claims claims = getClaims(token);
        String email = claims.get(JwtClaimConstants.EMAIL, String.class);
        if (email == null || email.isBlank()) {
            email = claims.getSubject();
        }
        return email;
    }

    public String extractRole(String token) {
        Claims claims = getClaims(token);
        Object role = claims.get(JwtClaimConstants.ROLE);
        return role != null ? role.toString() : null;
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims claims = getClaims(token);
        Object rolesObj = claims.get(JwtClaimConstants.ROLES);
        if (rolesObj instanceof List<?> list) {
            return list.stream().map(Object::toString).toList();
        }
        String singleRole = extractRole(token);
        return singleRole != null ? List.of(singleRole) : Collections.emptyList();
    }

    public Set<String> extractPermissions(String token) {
        Claims claims = getClaims(token);
        Object permissions = claims.get(JwtClaimConstants.PERMISSIONS);
        if (permissions instanceof Collection<?> coll) {
            return coll.stream().map(String::valueOf).collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    public String extractJti(String token) {
        Claims claims = getClaims(token);
        return claims.getId();
    }

    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getCredentials() == null) {
            throw new UnauthorizedException("User is not authenticated");
        }
        String token = authentication.getCredentials().toString();
        Long userId = extractUserId(token);
        if (userId == null) {
            throw new UnauthorizedException("User ID not found in token");
        }
        return userId;
    }

    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        return authentication.getName();
    }

    public List<String> getCurrentUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return Collections.emptyList();
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
    }
}
