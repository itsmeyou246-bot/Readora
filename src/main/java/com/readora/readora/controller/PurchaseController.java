package com.readora.readora.controller;

import com.readora.readora.model.Purchase;
import com.readora.readora.model.User;
import com.readora.readora.repository.PurchaseRepository;
import com.readora.readora.service.LibraryService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

    private final PurchaseRepository purchaseRepository;
    private final LibraryService libraryService;

    public PurchaseController(PurchaseRepository purchaseRepository, LibraryService libraryService) {
        this.purchaseRepository = purchaseRepository;
        this.libraryService = libraryService;
    }

    @GetMapping
    public List<Map<String, Object>> getMyPurchases(Authentication authentication) {
        User user = currentUser(authentication);
        return purchaseRepository.findByUserOrderByPurchasedAtDesc(user).stream()
                .map(p -> Map.<String, Object>of(
                        "id", p.getId(),
                        "bookTitle", p.getBook().getTitle(),
                        "price", p.getBook().getPrice() != null ? p.getBook().getPrice() : 0,
                        "purchasedAt", p.getPurchasedAt().toString().substring(0, 10)
                ))
                .toList();
    }

    private User currentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required.");
        }
        return libraryService.user(authentication.getName());
    }
}