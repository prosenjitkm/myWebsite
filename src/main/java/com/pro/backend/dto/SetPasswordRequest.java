package com.pro.backend.dto;

import com.pro.backend.constants.FieldConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for POST /api/auth/set-password
 *
 * - currentPassword: required only if the user already has a password (changing it).
 *                    Leave blank / null if this is a first-time password set (OAuth user).
 * - newPassword:     the password to set.
 */
public record SetPasswordRequest(

        String currentPassword,   // nullable — only required when user already has a password

        @NotBlank
        @Size(min = FieldConstants.PASSWORD_MIN, max = FieldConstants.PASSWORD_MAX,
              message = "Password must be between " + FieldConstants.PASSWORD_MIN
                      + " and " + FieldConstants.PASSWORD_MAX + " characters")
        String newPassword
) {}

