package com.pro.backend.service;

import com.pro.backend.constants.ServiceConstants;
import com.pro.backend.dto.AuthResponse;
import com.pro.backend.dto.LoginRequest;
import com.pro.backend.dto.RegisterRequest;
import com.pro.backend.entity.Role;
import com.pro.backend.entity.User;
import com.pro.backend.repository.RoleRepository;
import com.pro.backend.repository.UserRepository;
import com.pro.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new IllegalArgumentException(ServiceConstants.ERR_EMAIL_IN_USE);
        }
        if (userRepository.existsByUsername(req.username())) {
            throw new IllegalArgumentException(ServiceConstants.ERR_USERNAME_TAKEN);
        }

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
        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getEmail(), user.getUsername(), userRole.getName());
    }

    public AuthResponse login(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password()));

        User user = userRepository.findByEmail(req.email())
                .orElseThrow();

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getEmail(), user.getUsername(), user.getRole().getName());
    }
}

