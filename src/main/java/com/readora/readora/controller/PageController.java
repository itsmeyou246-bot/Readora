package com.readora.readora.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Controller
public class PageController {

    private static final Map<String, String> PAGES = Map.ofEntries(
            Map.entry("index", "index"),
            Map.entry("login", "login"),
            Map.entry("signup", "signup"),
            Map.entry("dashboard", "dashboard"),
            Map.entry("admin-dashboard", "admin-dashboard"),
            Map.entry("author-dashboard", "author-dashboard"),
            Map.entry("Book", "Book"),
            Map.entry("book", "Book"),
            Map.entry("book-details", "book-details"),
            Map.entry("library", "library"),
            Map.entry("my-library", "library"),
            Map.entry("bookmarks", "bookmarks"),
            Map.entry("cart", "cart"),
            Map.entry("checkout", "checkout"),
            Map.entry("purchase-history", "purchase-history"),
            Map.entry("purchase-success", "purchase-success"),
            Map.entry("journals", "journals"),
            Map.entry("magazines", "magazines"),
            Map.entry("magazine-details", "magazine-details"),
            Map.entry("newspapers", "newspapers"),
            Map.entry("newspaper-details", "newspaper-details"),
            Map.entry("notes", "notes"),
            Map.entry("read-nepal", "read-nepal"),
            Map.entry("nepal", "read-nepal"),
            Map.entry("read-nepal-details", "read-nepal-details"),
            Map.entry("reader", "reader"),
            Map.entry("search", "search")
    );

    @GetMapping({"/{page}", "/{page}.html"})
    public String page(@PathVariable String page) {
        String template = PAGES.get(page);
        if (template == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return template;
    }
}
