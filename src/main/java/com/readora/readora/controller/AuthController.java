package com.readora.readora.controller;

import com.readora.readora.dto.LoginRequest;
import com.readora.readora.dto.LoginResponse;
import com.readora.readora.dto.RegisterRequest;
import com.readora.readora.model.Role;
import com.readora.readora.model.User;
import com.readora.readora.repository.UserRepository;
import com.readora.readora.security.JwtService;
import com.readora.readora.service.EmailService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseCookie;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            EmailService emailService
    ) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }


    // =========================
    // REGISTER
    // =========================

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody RegisterRequest request
    ) {

        String name = request.getName()
                .trim();

        String email = request.getEmail()
                .trim()
                .toLowerCase();

        String password = request.getPassword();

        String selectedRole = request.getRole()
                .trim()
                .toUpperCase();


        // Check if email already exists

        if (userRepository
                .findByEmailIgnoreCase(email)
                .isPresent()) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "Email already registered."
                            )
                    );
        }


        // Convert selected role to Role enum

        Role role;

        try {

            role = Role.valueOf(selectedRole);

        } catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "Invalid role selected."
                            )
                    );
        }


        // Create new user

        User user = new User();

        user.setName(name);

        user.setEmail(email);

        // Encrypt password before saving

        user.setPassword(
                passwordEncoder.encode(password)
        );

        user.setRole(role);


        // Save user to database

        userRepository.save(user);


        // Registration successful

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Registration successful. Please login."
                )
        );
    }


    // =========================
    // LOGIN
    // =========================

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request
    ) {

        String email = request.getEmail()
                .trim()
                .toLowerCase();

        String password = request.getPassword();

        String selectedRole = request.getRole()
                .trim()
                .toUpperCase();


        // Find user by email

        Optional<User> optionalUser =
                userRepository.findByEmailIgnoreCase(email);


        if (optionalUser.isEmpty()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            Map.of(
                                    "message",
                                    "Email or password is incorrect."
                            )
                    );
        }


        User user = optionalUser.get();


        // Check password

        boolean passwordCorrect =
                passwordEncoder.matches(
                        password,
                        user.getPassword()
                );


        if (!passwordCorrect) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            Map.of(
                                    "message",
                                    "Email or password is incorrect."
                            )
                    );
        }


        // Check selected role

        if (!user.getRole()
                .name()
                .equals(selectedRole)) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            Map.of(
                                    "message",
                                    "The selected role does not match your account."
                            )
                    );
        }


        // Get user role

        String roleName = user.getRole().name();
        String role = roleName.toLowerCase();


        // Select dashboard based on role

        String dashboard;

        if (role.equals("admin")) {

            dashboard = "admin-dashboard.html";

        } else if (role.equals("author")) {

            dashboard = "author-dashboard.html";

        } else {

            dashboard = "dashboard.html";
        }


        // Generate JWT token

        String token =
                jwtService.generateToken(
                        user.getEmail(),
                        role
                );


        // Send login success email

        boolean emailSent =
                emailService.sendLoginSuccessEmail(
                        user.getEmail(),
                        user.getName(),
                        role
                );


        // Create login response

        LoginResponse response =
                new LoginResponse(
                        "Login successful.",
                        token,
                        user.getName(),
                        user.getEmail(),
                        roleName,
                        dashboard,
                        emailSent
                );


        ResponseCookie authCookie = ResponseCookie.from("readoraToken", token)
                .httpOnly(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(60 * 60)
                .build();
        return ResponseEntity.ok()
                .header("Set-Cookie", authCookie.toString())
                .body(response);
    }
}