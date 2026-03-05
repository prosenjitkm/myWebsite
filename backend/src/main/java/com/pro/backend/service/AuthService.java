package com.pro.backend.service;

import com.pro.backend.constants.ServiceConstants;
import com.pro.backend.dto.*;
import com.pro.backend.entity.Role;
import com.pro.backend.entity.User;
import com.pro.backend.repository.RoleRepository;
import com.pro.backend.repository.UserRepository;
import com.pro.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository        userRepository;
    private final RoleRepository        roleRepository;
    private final PasswordEncoder       passwordEncoder;
    private final JwtService            jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        log.debug("register() — checking email={}", req.email());
        if (userRepository.existsByEmail(req.email()))
            throw new IllegalArgumentException(ServiceConstants.ERR_EMAIL_IN_USE);
        if (userRepository.existsByUsername(req.username()))
            throw new IllegalArgumentException(ServiceConstants.ERR_USERNAME_TAKEN);

        Role userRole = roleRepository.findByName(ServiceConstants.ROLE_USER)
                .orElseThrow(() -> new IllegalStateException(ServiceConstants.ERR_USER_ROLE_NOT_FOUND));

        User user = User.builder()
                .email(req.email())
                .passwordHash(passwordEncoder.encode(req.password()))
                .username(req.username())
                .firstName(req.firstName())
                .lastName(req.lastName())
                .role(userRole)
                .isActive(true)
                .build();

        userRepository.save(user);
        log.info("User registered — email={} username={}", user.getEmail(), user.getUsername());
        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getEmail(), user.getUsername(), userRole.getName());
    }

    public AuthResponse login(LoginRequest req) {
        log.debug("login() — authenticating email={}", req.email());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        User user = userRepository.findByEmail(req.email()).orElseThrow();
        log.info("login() — success email={} role={}", user.getEmail(), user.getRole().getName());
        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getEmail(), user.getUsername(), user.getRole().getName());
    }

    /** Returns the public profile of the currently authenticated user. */
    public UserResponse getMe(String email) {
        log.debug("getMe() — email={}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return new UserResponse(
                user.getEmail(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().getName(),
                user.getPasswordHash() != null && !user.getPasswordHash().isBlank(),
                user.getOauthProvider()
        );
    }

    /**
     * Set or change a password for the authenticated user.
     *
     * Rules:
     *  - If user has NO password (OAuth-only): just set the new one — no currentPassword needed.
     *  - If user ALREADY has a password: currentPassword must be provided and correct.
     */
    @Transactional
    public void setPassword(String email, SetPasswordRequest req) {
        log.debug("setPassword() — email={}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        boolean alreadyHasPassword = user.getPasswordHash() != null
                                  && !user.getPasswordHash().isBlank();

        if (alreadyHasPassword) {
            // Changing existing password — verify current one first
            if (req.currentPassword() == null || req.currentPassword().isBlank()) {
                throw new BadCredentialsException("Current password is required to set a new one.");
            }
            if (!passwordEncoder.matches(req.currentPassword(), user.getPasswordHash())) {
                log.warn("setPassword() — wrong current password for email={}", email);
                throw new BadCredentialsException("Current password is incorrect.");
            }
        }
        // First-time set (OAuth user) — no current password check needed

        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        user.setUpdatedAt(OffsetDateTime.now());
        userRepository.save(user);
        log.info("setPassword() — password updated for email={} firstTime={}", email, !alreadyHasPassword);
    }
}
