package com.plug.security.evaluator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * Evaluador de permisos personalizado que verifica tanto authorities como scopes en JWT.
 * Permite la validación de permisos mediante @PreAuthorize con hasPermission().
 */
@Slf4j
@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {
    
    @Override
    public boolean hasPermission(Authentication authentication, Object privilegeRequired, Object scopeRequired) {
        log.debug("Evaluating permission - Privilege: {}, Scope: {}", privilegeRequired, scopeRequired);

        if (authentication == null || !authentication.isAuthenticated()) {
            log.debug("Authentication is null or not authenticated");
            return false;
        }

        if (!(privilegeRequired instanceof String) || !(scopeRequired instanceof String)) {
            log.debug("Invalid privilege or scope type");
            return false;
        }

        String privilege = (String) privilegeRequired;
        String scope = (String) scopeRequired;

        boolean hasPrivilege = hasAuthority(authentication, privilege);
        boolean hasScope = hasScope(authentication, scope);

        log.debug("Has privilege '{}': {}, Has scope '{}': {}", privilege, hasPrivilege, scope, hasScope);

        return hasPrivilege || hasScope;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        log.debug("hasPermission(targetId) not implemented - returning false");
        return false;
    }

    private boolean hasAuthority(Authentication authentication, String privilegeRequired) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if (authorities == null || authorities.isEmpty()) {
            return false;
        }

        for (GrantedAuthority authority : authorities) {
            if (authority.getAuthority().equalsIgnoreCase(privilegeRequired)) {
                log.debug("Found matching authority: {}", authority.getAuthority());
                return true;
            }
        }

        return false;
    }

    private boolean hasScope(Authentication authentication, String scopeRequired) {
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            Object scopeClaim = jwt.getClaims().get("scope");

            if (scopeClaim instanceof String scopeString) {
                List<String> scopes = List.of(scopeString.split(" "));
                boolean hasScope = scopes.stream()
                        .anyMatch(s -> s.equalsIgnoreCase(scopeRequired));

                if (hasScope) {
                    log.debug("Found matching scope in JWT: {}", scopeRequired);
                }

                return hasScope;
            } else if (scopeClaim instanceof Collection) {
                @SuppressWarnings("unchecked")
                Collection<String> scopes = (Collection<String>) scopeClaim;
                boolean hasScope = scopes.stream()
                        .anyMatch(s -> s.equalsIgnoreCase(scopeRequired));

                if (hasScope) {
                    log.debug("Found matching scope in JWT: {}", scopeRequired);
                }

                return hasScope;
            }
        }

        log.debug("No JWT authentication or scope claim found, allowing access");
        return true;
    }
}
