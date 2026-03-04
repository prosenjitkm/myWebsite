package com.pro.backend.dto;

public record AuthResponse(String token, String email, String username, String role) {}

