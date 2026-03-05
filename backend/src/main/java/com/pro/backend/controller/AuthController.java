package com.pro.backend.controller;

import com.pro.backend.constants.UrlConstants;
import com.pro.backend.dto.*;
import com.pro.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(UrlConstants.Auth.BASE)
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping(UrlConstants.Auth.REGISTER)
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("POST /register — email={}", request.email());
        try {
            AuthResponse response = authService.register(request);
            log.info("Register success — username={}", response.username());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Register failed — email={} reason={}", request.email(), e.getMessage());
            throw e;
        }
    }

    @PostMapping(UrlConstants.Auth.LOGIN)
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /login — email={}", request.email());
        try {
            AuthResponse response = authService.login(request);
            log.info("Login success — email={} role={}", response.email(), response.role());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.warn("Login failed — email={} reason={}", request.email(), e.getMessage());
            throw e;
        }
    }

    /** Returns the profile of whoever is currently logged in (any authenticated user). */
    @GetMapping(UrlConstants.Auth.ME)
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal UserDetails principal) {
        log.debug("GET /me — user={}", principal.getUsername());
        return ResponseEntity.ok(authService.getMe(principal.getUsername()));
    }

    /**
     * Set password for the first time (OAuth user), or change existing password.
     * Requires the user to be logged in — the JWT identifies who is making the request.
     */
    @PostMapping(UrlConstants.Auth.SET_PASSWORD)
    public ResponseEntity<Void> setPassword(@AuthenticationPrincipal UserDetails principal,
                                            @Valid @RequestBody SetPasswordRequest request) {
        log.info("POST /set-password — user={}", principal.getUsername());
        try {
            authService.setPassword(principal.getUsername(), request);
            log.info("Password set/changed successfully — user={}", principal.getUsername());
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Set-password failed — user={} reason={}", principal.getUsername(), e.getMessage());
            throw e;
        }
    }
}
