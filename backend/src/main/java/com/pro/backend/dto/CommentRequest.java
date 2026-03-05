package com.pro.backend.dto;

import com.pro.backend.constants.FieldConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CommentRequest(
        @NotBlank @Size(min = FieldConstants.COMMENT_BODY_MIN, max = FieldConstants.COMMENT_BODY_MAX) String body,
        UUID parentId   // null for top-level comment
) {}

