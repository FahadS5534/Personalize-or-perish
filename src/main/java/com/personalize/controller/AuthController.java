package com.personalize.controller;

import com.personalize.dto.auth.AuthResponse;
import com.personalize.dto.auth.LoginRequest;
import com.personalize.dto.auth.RegisterRequest;
import com.personalize.entity.Role;
import com.personalize.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/guest")
    public ResponseEntity<AuthResponse> guestLogin(@RequestParam(required = false, defaultValue = "STUDENT") Role role) {
        return ResponseEntity.ok(authService.guestLogin(role));
    }
}
