package com.pro.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        String body,
        String authorUsername,
        UUID parentId,
        OffsetDateTime createdAt
) {}

