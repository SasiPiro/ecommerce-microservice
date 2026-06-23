package com.ecommerce.product.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

@Component
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_PERMISSIONS = "X-User-Permissions";
    private static final String HEADER_USER_ROLES = "X-User-Roles";
    private static final String HEADER_USERNAME = "X-Username";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String userId = request.getHeader(HEADER_USER_ID);
        String username = request.getHeader(HEADER_USERNAME);
        String userRoles = request.getHeader(HEADER_USER_ROLES);
        String userPermissions = request.getHeader(HEADER_USER_PERMISSIONS);

        //Se non ci sono header skip (endpoint pubblico o chiamata diretta)
        if(username == null || userId== null){
            filterChain.doFilter(request,response);
            return;
        }

        // Costruisci authorities
        Collection<GrantedAuthority> authorities = buildAuthorities(userRoles, userPermissions);

        // Popola SecurityContext
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        username,  // principal
                        null,      // credentials (no pwd)
                        authorities);

        authToken.setDetails(userId);
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }

    private static Collection<GrantedAuthority> buildAuthorities(String userRoles, String userPermissions) {
        Collection<GrantedAuthority> authorities = new HashSet<>();

        // Gestione Ruoli -> aggiunge prefisso ROLE_
        if (userRoles != null && !userRoles.isEmpty()) {
            Arrays.stream(userRoles.split(","))
                    .map(String::trim)
                    .filter(r -> !r.isEmpty())
                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                    .forEach(authorities::add);
        }
        // Gestione Permessi -> autorità diretta
        if (userPermissions != null && !userPermissions.isEmpty()) {
            Arrays.stream(userPermissions.split(","))
                    .map(String::trim)
                    .filter(p -> !p.isEmpty())
                    .map(SimpleGrantedAuthority::new)
                    .forEach(authorities::add);
        }
        return authorities;
    }
}
