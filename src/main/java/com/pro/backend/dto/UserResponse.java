package com.pro.backend.dto;

/**
 * Returned by GET /api/auth/me — safe public profile of the logged-in user.
 * hasPassword lets the frontend know whether to show "Set Password" or "Change Password".
 */
public record UserResponse(
        String email,
        String username,
        String firstName,
        String lastName,
        String role,
        boolean hasPassword,      // false = OAuth-only account, never set a password
        String oauthProvider      // e.g. "google", or null for local accounts
) {}

