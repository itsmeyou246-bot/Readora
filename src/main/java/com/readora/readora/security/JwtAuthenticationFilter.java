package com.readora.readora.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() == null) {

            /*
             * Collect every token the browser sent:
             *
             *   1) Authorization: Bearer <token>   (API calls from JavaScript)
             *   2) readoraToken cookie             (page navigation + fallback)
             *
             * FIX: the old filter used the Authorization header ONLY when it
             * was present. A stale / expired token left in localStorage was
             * therefore sent as the header, rejected, and the valid cookie
             * was never tried - every API call failed and pages stayed on
             * "Loading...". Now the first VALID token wins.
             */
            List<String> candidates = new ArrayList<>();

            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                candidates.add(header);
            }

            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("readoraToken".equals(cookie.getName())) {
                        String value = cookie.getValue();
                        if (value != null && !value.isBlank()) {
                            candidates.add("Bearer " + value);
                        }
                        break;
                    }
                }
            }

            for (String candidate : candidates) {

                Optional<JwtService.ClaimsData> claims =
                        jwtService.extractClaims(candidate);

                if (claims.isPresent()) {
                    authenticate(request, claims.get());
                    break;
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private void authenticate(
            HttpServletRequest request,
            JwtService.ClaimsData claims) {

        String role = claims.role();

        List<SimpleGrantedAuthority> authorities;

        if (role == null || role.isBlank()) {
            authorities = List.of();
        } else {
            authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())
            );
        }

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        claims.email(),
                        null,
                        authorities
                );

        authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}