package com.readora.readora.controller;

import com.readora.readora.dto.LoginRequest;
import com.readora.readora.dto.LoginResponse;
import com.readora.readora.dto.RegisterRequest;
import com.readora.readora.model.Role;
import com.readora.readora.model.User;
import com.readora.readora.repository.UserRepository;
import com.readora.readora.security.JwtService;
import com.readora.readora.service.EmailService;

import jakarta.validation.Valid;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.web.bind.MethodArgumentNotValidException;
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
            EmailService emailService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }


    // =====================================================
    // REGISTER
    // =====================================================

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest request) {

        try {

            // ---------------------------------------------
            // GET AND CLEAN INPUT
            // ---------------------------------------------

            if (request.getName() == null ||
                    request.getName().trim().isEmpty()) {

                return ResponseEntity
                        .badRequest()
                        .body(Map.of(
                                "message",
                                "Full name is required."
                        ));
            }


            if (request.getEmail() == null ||
                    request.getEmail().trim().isEmpty()) {

                return ResponseEntity
                        .badRequest()
                        .body(Map.of(
                                "message",
                                "Email is required."
                        ));
            }


            if (request.getPassword() == null ||
                    request.getPassword().isEmpty()) {

                return ResponseEntity
                        .badRequest()
                        .body(Map.of(
                                "message",
                                "Password is required."
                        ));
            }


            if (request.getRole() == null ||
                    request.getRole().trim().isEmpty()) {

                return ResponseEntity
                        .badRequest()
                        .body(Map.of(
                                "message",
                                "Please select a role."
                        ));
            }


            String name =
                    request.getName().trim();

            String email =
                    request.getEmail()
                            .trim()
                            .toLowerCase();

            String password =
                    request.getPassword();

            String selectedRole =
                    request.getRole()
                            .trim()
                            .toUpperCase();


            // ---------------------------------------------
            // CHECK DUPLICATE EMAIL
            // ---------------------------------------------

            if (userRepository
                    .findByEmailIgnoreCase(email)
                    .isPresent()) {

                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(Map.of(
                                "message",
                                "Email already registered. Please use another email or login."
                        ));
            }


            // ---------------------------------------------
            // PREVENT ADMIN REGISTRATION
            // ---------------------------------------------

            if ("ADMIN".equals(selectedRole)) {

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(Map.of(
                                "message",
                                "Admin accounts cannot be created from public signup."
                        ));
            }


            // ---------------------------------------------
            // CHECK ROLE
            // ---------------------------------------------

            Role role;

            try {

                role = Role.valueOf(selectedRole);

            } catch (IllegalArgumentException e) {

                return ResponseEntity
                        .badRequest()
                        .body(Map.of(
                                "message",
                                "Invalid role selected. Please select Reader or Author."
                        ));
            }


            // ---------------------------------------------
            // CREATE USER
            // ---------------------------------------------

            User user = new User();

            user.setName(name);

            user.setEmail(email);

            user.setPassword(
                    passwordEncoder.encode(password)
            );

            user.setRole(role);


            // ---------------------------------------------
            // SAVE USER
            // ---------------------------------------------

            userRepository.save(user);


            // ---------------------------------------------
            // SUCCESS
            // ---------------------------------------------

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(Map.of(
                            "message",
                            "Registration successful. Please login."
                    ));

        } catch (DataIntegrityViolationException e) {

            // ---------------------------------------------
            // DATABASE DUPLICATE / CONSTRAINT ERROR
            // ---------------------------------------------

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "message",
                            "This email is already registered."
                    ));

        } catch (Exception e) {

            // ---------------------------------------------
            // OTHER SERVER ERROR
            // ---------------------------------------------

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message",
                            "Registration failed because of a server error: "
                                    + e.getClass().getSimpleName()
                    ));
        }
    }


    // =====================================================
    // VALIDATION ERROR
    // =====================================================

    @ExceptionHandler(
            MethodArgumentNotValidException.class
    )
    public ResponseEntity<?> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        String message =
                ex.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .findFirst()
                        .map(error -> {

                            String defaultMessage =
                                    error.getDefaultMessage();

                            return defaultMessage != null
                                    ? defaultMessage
                                    : "Please check your input.";

                        })
                        .orElse(
                                "Please check your input."
                        );


        return ResponseEntity
                .badRequest()
                .body(
                        Map.of(
                                "message",
                                message
                        )
                );
    }


    // =====================================================
    // LOGIN
    // =====================================================

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request) {

        String email =
                request.getEmail()
                        .trim()
                        .toLowerCase();

        String password =
                request.getPassword();

        String selectedRole =
                request.getRole()
                        .trim()
                        .toUpperCase();


        Optional<User> optionalUser =
                userRepository
                        .findByEmailIgnoreCase(email);


        // ---------------------------------------------
        // USER NOT FOUND
        // ---------------------------------------------

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


        User user =
                optionalUser.get();


        // ---------------------------------------------
        // CHECK PASSWORD
        // ---------------------------------------------

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


        // ---------------------------------------------
        // CHECK ROLE
        // ---------------------------------------------

        if (user.getRole() == null ||
                !user.getRole()
                        .name()
                        .equalsIgnoreCase(selectedRole)) {

            String actualRole =
                    user.getRole() == null
                            ? "UNKNOWN"
                            : user.getRole().name();

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            Map.of(
                                    "message",
                                    "The selected role does not match your account. "
                                            + "Your account is registered as "
                                            + actualRole + "."
                            )
                    );
        }


        String roleName =
                user.getRole().name();

        String role =
                roleName.toLowerCase();


        // ---------------------------------------------
        // SELECT DASHBOARD
        // ---------------------------------------------

        String dashboard;

        if ("admin".equals(role)) {

            dashboard = "/admin-dashboard";

        } else if ("author".equals(role)) {

            dashboard = "/author-dashboard";

        } else {

            dashboard = "/dashboard";
        }


        // ---------------------------------------------
        // GENERATE JWT
        // ---------------------------------------------

        String token =
                jwtService.generateToken(
                        user.getEmail(),
                        role
                );


        // ---------------------------------------------
        // SEND LOGIN EMAIL
        // ---------------------------------------------

        boolean emailSent =
                emailService
                        .sendLoginSuccessEmail(
                                user.getEmail(),
                                user.getName(),
                                role
                        );


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


        // ---------------------------------------------
        // JWT COOKIE
        // ---------------------------------------------

        ResponseCookie authCookie =
                ResponseCookie
                        .from(
                                "readoraToken",
                                token
                        )
                        .httpOnly(true)
                        .sameSite("Lax")
                        .path("/")
                        .maxAge(60 * 60 * 24)
                        .build();


        return ResponseEntity
                .ok()
                .header(
                        "Set-Cookie",
                        authCookie.toString()
                )
                .body(response);
    }
}