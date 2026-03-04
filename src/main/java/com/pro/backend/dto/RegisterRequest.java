package com.pro.backend.dto;

import com.pro.backend.constants.FieldConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Email @NotBlank String email,
        @NotBlank @Size(min = FieldConstants.PASSWORD_MIN, max = FieldConstants.PASSWORD_MAX) String password,
        @NotBlank @Size(min = FieldConstants.USERNAME_MIN, max = FieldConstants.USERNAME_MAX) String username,
        @NotBlank @Size(max = FieldConstants.FIRST_NAME_MAX) String firstName,
        @NotBlank @Size(max = FieldConstants.LAST_NAME_MAX) String lastName
) {}
