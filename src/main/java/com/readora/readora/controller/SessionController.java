package com.readora.readora.controller;

import com.readora.readora.model.User;
import com.readora.readora.repository.UserRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * SESSION ENDPOINTS
 *
 *   POST /api/auth/logout  -> clears the HttpOnly JWT cookie
 *   GET  /logout           -> same, then redirects to /login
 *   GET  /api/auth/me      -> who is logged in right now (401 if nobody)
 *
 * The JWT lives in an HttpOnly cookie, so JavaScript cannot delete it.
 * Logout therefore has to be done by the server.
 */
@Controller
public class SessionController {

    private final UserRepository users;

    public SessionController(UserRepository users) {
        this.users = users;
    }

    private ResponseCookie clearedCookie() {
        return ResponseCookie.from("readoraToken", "")
                .httpOnly(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
    }

    @PostMapping("/api/auth/logout")
    @ResponseBody
    public ResponseEntity<Map<String, String>> logout() {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearedCookie().toString())
                .body(Map.of("message", "Logged out."));
    }

    @GetMapping("/logout")
    public ResponseEntity<Void> logoutAndRedirect() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.SET_COOKIE, clearedCookie().toString())
                .header(HttpHeaders.LOCATION, "/login")
                .build();
    }

    @GetMapping("/api/auth/me")
    @ResponseBody
    public ResponseEntity<?> me(Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Not logged in."));
        }

        User user = users.findByEmailIgnoreCase(authentication.getName()).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "User not found."));
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", user.getId());
        body.put("name", user.getName() == null ? "" : user.getName());
        body.put("email", user.getEmail());
        body.put("role", user.getRole() == null ? "USER" : user.getRole().name());
        return ResponseEntity.ok(body);
    }
}