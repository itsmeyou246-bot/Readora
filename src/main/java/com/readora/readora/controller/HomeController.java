package com.readora.readora.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "signup";
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    @GetMapping("/subscription")
    public String subscription() {
        return "subscription";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/publish-book")
    public String publishBook() {
        return "publish-book";
    }

    @GetMapping("/author-dashboard")
    public String authorDashboard() {
        return "AuthorDashboard";
    }
}