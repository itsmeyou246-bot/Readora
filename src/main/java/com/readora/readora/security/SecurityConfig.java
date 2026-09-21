package com.readora.readora.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) throws Exception {

        http
                // =====================================
                // JWT APPLICATION SETTINGS
                // =====================================
                .csrf(csrf -> csrf.disable())

                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // =====================================
                // NOT LOGGED IN / WRONG ROLE
                // =====================================
                //
                // Before: an expired or missing login produced a blank
                // 403 error page on every admin / reader page and a silent
                // failure on every API call ("Loading..." forever).
                //
                // Now:
                //   pages  -> redirect to /login (or to the user's own home)
                //   /api/* -> clean 401 / 403 JSON
                //
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            if (request.getRequestURI().startsWith("/api/")) {
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                response.setContentType("application/json");
                                response.getWriter().write(
                                        "{\"message\":\"Authentication is required.\"}");
                            } else {
                                response.sendRedirect("/login");
                            }
                        })
                        .accessDeniedHandler((request, response, deniedException) -> {
                            if (request.getRequestURI().startsWith("/api/")) {
                                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                response.setContentType("application/json");
                                response.getWriter().write(
                                        "{\"message\":\"You do not have permission for this action.\"}");
                            } else {
                                response.sendRedirect(homeFor(
                                        SecurityContextHolder.getContext().getAuthentication()));
                            }
                        })
                )

                // =====================================
                // AUTHORIZATION RULES
                // =====================================
                .authorizeHttpRequests(auth -> auth

                        // =====================================
                        // PUBLIC ROOT AND AUTHENTICATION PAGES
                        // =====================================
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/signup",
                                "/signup.html",
                                "/login",
                                "/login.html",
                                "/logout",
                                "/subscription",
                                "/subscription.html"
                        ).permitAll()

                        // =====================================
                        // PUBLIC CATALOG PAGES
                        // =====================================
                        .requestMatchers(
                                "/Book",
                                "/Book.html",
                                "/book",
                                "/book.html",
                                "/book-details",
                                "/book-details.html",
                                "/journals",
                                "/journals.html",
                                "/magazines",
                                "/magazines.html",
                                "/magazine-details",
                                "/magazine-details.html",
                                "/newspapers",
                                "/newspapers.html",
                                "/newspaper-details",
                                "/newspaper-details.html",
                                "/read-nepal",
                                "/read-nepal.html",
                                "/nepal",
                                "/nepal.html",
                                "/read-nepal-details",
                                "/read-nepal-details.html",
                                "/search",
                                "/search.html"
                        ).permitAll()

                        // =====================================
                        // PUBLIC SHOPPING PAGES
                        // =====================================
                        //
                        // The cart can be viewed without login.
                        // Checkout page can also open without login.
                        // The purchase API below still requires login.
                        //
                        .requestMatchers(
                                "/cart",
                                "/cart.html",
                                "/checkout",
                                "/checkout.html",
                                "/purchase-success",
                                "/purchase-success.html"
                        ).permitAll()

                        // =====================================
                        // PUBLIC STATIC RESOURCES
                        // =====================================
                        .requestMatchers(
                                "/css/**",
                                "/js/**",
                                "/img/**",
                                "/images/**",
                                "/books/**",
                                "/uploads/**",
                                "/fonts/**",
                                "/webjars/**",
                                "/favicon.ico"
                        ).permitAll()

                        // =====================================
                        // PUBLIC AUTHENTICATION APIs
                        // =====================================
                        .requestMatchers(
                                "/api/auth/**"
                        ).permitAll()

                        // =====================================
                        // PUBLIC ESEWA CALLBACK URLs
                        // =====================================
                        //
                        // eSewa's own redirect hits these directly, carrying
                        // its own signed transaction_uuid/data payload for
                        // verification, so they must not require a valid
                        // session - if the browser's session cookie happened
                        // to expire during checkout, these would otherwise
                        // get blocked before ever reaching PaymentController.
                        //
                        .requestMatchers(
                                "/api/payments/esewa/success",
                                "/api/payments/esewa/failure"
                        ).permitAll()

                        // =====================================
                        // PUBLIC BOOK AND SEARCH APIs
                        // =====================================
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/books/**",
                                "/api/reviews/book/**",
                                "/api/mood/recommendations"
                        ).permitAll()

                        // =====================================
                        // PUBLIC READER PAGE
                        // =====================================
                        //
                        // The reader page is publicly reachable.
                        // BookReaderController checks whether:
                        //
                        // FREE BOOK    -> allowed
                        // PREMIUM BOOK -> purchase required
                        //
                        .requestMatchers(
                                "/book-reader",
                                "/book-reader.html"
                        ).permitAll()

                        // =====================================
                        // READER ACCESS APIs
                        // =====================================
                        //
                        // These requests must reach the controller.
                        // The controller performs the actual free/
                        // premium ownership check.
                        //
                        .requestMatchers(
                                "/api/reader/**"
                        ).permitAll()

                        // =====================================
                        // USER PAGES
                        // =====================================
                        .requestMatchers(
                                "/dashboard",
                                "/dashboard.html",
                                "/library",
                                "/library.html",
                                "/my-library",
                                "/my-library.html",
                                "/bookmarks",
                                "/bookmarks.html",
                                "/notes",
                                "/notes.html",
                                "/purchase-history",
                                "/purchase-history.html",
                                "/reader/**"
                        ).hasAnyRole(
                                "USER",
                                "AUTHOR",
                                "ADMIN"
                        )

                        // =====================================
                        // AUTHOR PAGES AND APIs
                        // =====================================
                        .requestMatchers(
                                "/AuthorDashboard",
                                "/AuthorDashboard.html",
                                "/author-dashboard",
                                "/author-dashboard.html",
                                "/MyPublication",
                                "/MyPublication.html",
                                "/CreateNewPublication",
                                "/CreateNewPublication.html",
                                "/SubmitManuscript",
                                "/SubmitManuscript.html",
                                "/submit-manuscript",
                                "/submit-manuscript.html",
                                "/SubmissionandPipeline",
                                "/SubmissionandPipeline.html",
                                "/RoyalitiesandSales",
                                "/RoyalitiesandSales.html",
                                "/ReaderAnalytics",
                                "/ReaderAnalytics.html",
                                "/EditorialGuidelines",
                                "/EditorialGuidelines.html",
                                "/AuthorProfile",
                                "/AuthorProfile.html",
                                "/author-profile",
                                "/author-profile.html",
                                "/api/author/**",
                                "/api/editorial-guidelines/**"
                        ).hasAnyRole(
                                "AUTHOR",
                                "ADMIN"
                        )

                        // =====================================
                        // ADMIN PAGES AND APIs
                        // =====================================
                        .requestMatchers(
                                "/admin-dashboard",
                                "/admin-dashboard.html",
                                "/admin/dashboard",
                                "/admin/dashboard.html",
                                "/admin/users",
                                "/admin/users.html",
                                "/admin-users",
                                "/admin-users.html",
                                "/admin/books",
                                "/admin/books.html",
                                "/admin/books/add",
                                "/admin/books/add.html",
                                "/admin/books/pages",
                                "/admin/books/pages.html",
                                "/admin/subscriptions",
                                "/admin/subscriptions.html",
                                "/admin/authors",
                                "/admin/authors.html",
                                "/admin/analytics",
                                "/admin/analytics.html",
                                "/admin-book",
                                "/admin-book.html",
                                "/admin-books",
                                "/admin-books.html",
                                "/admin-authors",
                                "/admin-authors.html",
                                "/admin-subscriptions",
                                "/admin-subscriptions.html",
                                "/admin-analytics",
                                "/admin-analytics.html",
                                "/analytics",
                                "/analytics.html",
                                "/admin/read-nepal",
                                "/admin/read-nepal.html",
                                "/admin/author-studio",
                                "/admin/author-studio.html",
                                "/add-content",
                                "/add-content.html",
                                "/api/admin/**"
                        ).hasRole("ADMIN")

                        // =====================================
                        // PRIVATE APIs
                        // =====================================
                        //
                        // Cart is stored in localStorage, so the
                        // cart page does not need authentication.
                        //
                        // However, purchases must be authenticated.
                        //
                        .requestMatchers(
                                "/api/dashboard",
                                "/api/library/**",
                                "/api/purchases/**",
                                "/api/reading-progress/**",
                                "/api/subscription/**",
                                "/api/recommendations/**",
                                "/api/mood/**",
                                "/api/analytics/**",
                                "/api/history/**",
                                "/api/bookmarks/**",
                                "/api/notes/**",
                                "/api/highlights/**",
                                "/api/notifications/**",
                                "/api/favorites/**",
                                "/api/reviews",
                                "/api/reviews/**",
                                "/api/upload"
                        ).authenticated()

                        // =====================================
                        // EVERYTHING ELSE
                        // =====================================
                        .anyRequest().authenticated()
                )

                // =====================================
                // JWT FILTER
                // =====================================
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    /**
     * Where a logged-in user is sent when they open a page that belongs to
     * another role (for example a reader opening /admin-dashboard).
     */
    private static String homeFor(Authentication authentication) {

        if (authentication != null) {
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                switch (authority.getAuthority()) {
                    case "ROLE_ADMIN":
                        return "/admin-dashboard";
                    case "ROLE_AUTHOR":
                        return "/AuthorDashboard";
                    case "ROLE_USER":
                        return "/dashboard";
                    default:
                        break;
                }
            }
        }
        return "/login";
    }
}