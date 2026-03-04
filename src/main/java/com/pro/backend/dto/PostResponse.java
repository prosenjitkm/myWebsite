package com.pro.backend.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record PostResponse(
        UUID id,
        String title,
        String slug,
        String summary,
        String body,
        String coverImage,
        List<String> tags,
        String authorUsername,
        OffsetDateTime publishedAt
) {}

