package com.readora.readora.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * ADMIN PAGE ROUTES
 *
 * Every link used by the admin header (fragments/header.html) and by the
 * older admin pages is mapped here, so no admin navigation link can
 * return a 404 error page.
 *
 * All of these URLs are protected in SecurityConfig (ADMIN role only).
 */
@Controller
public class AdminPageController {

    // ---------------- DASHBOARD ----------------
    @GetMapping({
            "/admin-dashboard",
            "/admin-dashboard.html",
            "/admin/dashboard",
            "/admin/dashboard.html"
    })
    public String adminDashboard() {
        return "admin-dashboard";
    }

    // ---------------- USERS ----------------
    @GetMapping({
            "/admin/users",
            "/admin/users.html",
            "/admin-users",
            "/admin-users.html"
    })
    public String adminUsersPage() {
        return "admin-users";
    }

    // ---------------- CONTENT / BOOKS ----------------
    @GetMapping({
            "/admin/books",
            "/admin/books.html",
            "/admin-book",
            "/admin-book.html",
            "/admin-books",
            "/admin-books.html"
    })
    public String booksPage() {
        return "admin-book";
    }

    @GetMapping({"/admin/books/add", "/admin/books/add.html"})
    public String addBookPage() {
        return "add-book";
    }

    // ---------------- SUBSCRIPTIONS ----------------
    @GetMapping({
            "/admin/subscriptions",
            "/admin/subscriptions.html",
            "/admin-subscriptions",
            "/admin-subscriptions.html"
    })
    public String subscriptionsPage() {
        return "admin-subscriptions";
    }

    // ---------------- AUTHORS ----------------
    @GetMapping({
            "/admin/authors",
            "/admin/authors.html",
            "/admin-authors",
            "/admin-authors.html"
    })
    public String authorsPage() {
        return "admin-authors";
    }

    // ---------------- ANALYTICS ----------------
    @GetMapping({
            "/admin/analytics",
            "/admin/analytics.html",
            "/admin-analytics",
            "/admin-analytics.html",
            "/analytics",
            "/analytics.html"
    })
    public String analytics() {
        return "analytics";
    }

    // ---------------------------------------------------------------
    // Old URLs that never had a template (they used to cause a
    // "Template might not exist" 500 error). They now forward to the
    // closest page that really exists.
    // ---------------------------------------------------------------
    @GetMapping({"/admin/books/pages", "/admin/books/pages.html"})
    public String addBookPages() {
        return "redirect:/admin/books/add";
    }

    @GetMapping({"/admin/read-nepal", "/admin/read-nepal.html"})
    public String adminReadNepal() {
        return "redirect:/add-content";
    }

    @GetMapping({"/admin/author-studio", "/admin/author-studio.html"})
    public String adminAuthorStudio() {
        return "redirect:/AuthorDashboard";
    }
}