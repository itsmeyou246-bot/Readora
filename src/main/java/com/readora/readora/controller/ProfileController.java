package com.readora.readora.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * PROFILE LINKS USED BY THE SHARED HEADER
 *
 *   /AuthorProfile, /author-profile  -> the existing AuthorProfile page
 *   /profile, /admin-profile         -> sends each role to its own home
 *
 * Without these mappings the "View Profile" links in the header
 * returned a 404 error page.
 */
@Controller
public class ProfileController {

    @GetMapping({
            "/AuthorProfile",
            "/AuthorProfile.html",
            "/author-profile",
            "/author-profile.html"
    })
    public String authorProfile() {
        return "AuthorProfile";
    }

    @GetMapping({
            "/profile",
            "/profile.html",
            "/admin-profile",
            "/admin-profile.html"
    })
    public String profile(Authentication authentication) {

        if (authentication != null) {
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                String role = authority.getAuthority();

                if ("ROLE_ADMIN".equals(role)) {
                    return "redirect:/admin-dashboard";
                }
                if ("ROLE_AUTHOR".equals(role)) {
                    return "redirect:/AuthorProfile";
                }
            }
        }
        return "redirect:/dashboard";
    }
}