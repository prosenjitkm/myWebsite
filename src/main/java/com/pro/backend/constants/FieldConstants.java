package com.pro.backend.constants;

/**
 * Maximum (and minimum) field lengths that must stay in sync between
 * the database schema, JPA entities, and DTO validation annotations.
 *
 * Rule: every @Column(length=…) and @Size(…) in the project
 * references a constant from here — never a magic number.
 */
public final class FieldConstants {

    private FieldConstants() {}   // utility class — no instances

    // ----------------------------------------------------------------
    // User fields
    // ----------------------------------------------------------------
    public static final int EMAIL_MAX        = 255;
    public static final int PASSWORD_HASH_MAX= 255;
    public static final int PASSWORD_MIN     = 8;
    public static final int PASSWORD_MAX     = 100;
    public static final int USERNAME_MIN     = 3;
    public static final int USERNAME_MAX     = 50;
    public static final int FIRST_NAME_MAX   = 100;
    public static final int LAST_NAME_MAX    = 100;

    // ----------------------------------------------------------------
    // Post fields
    // ----------------------------------------------------------------
    public static final int POST_TITLE_MAX       = 255;
    public static final int POST_SLUG_MAX        = 255;
    public static final int POST_COVER_IMAGE_MAX = 500;

    // ----------------------------------------------------------------
    // Comment fields
    // ----------------------------------------------------------------
    public static final int COMMENT_BODY_MIN = 1;
    public static final int COMMENT_BODY_MAX = 6000;

    // ----------------------------------------------------------------
    // Tag fields
    // ----------------------------------------------------------------
    public static final int TAG_NAME_MAX = 50;

    // ----------------------------------------------------------------
    // Resume section fields
    // ----------------------------------------------------------------
    public static final int RESUME_SECTION_MAX  = 50;
    public static final int RESUME_TITLE_MAX    = 255;
    public static final int RESUME_SUBTITLE_MAX = 255;
    public static final int RESUME_LOCATION_MAX = 255;
    // description uses PostgreSQL TEXT (unlimited) — no hard cap, practical max ~50 000 chars
    public static final int RESUME_DESCRIPTION_SOFT_MAX = 50_000;

    // ----------------------------------------------------------------
    // Role fields
    // ----------------------------------------------------------------
    public static final int ROLE_NAME_MAX = 20;

    // ----------------------------------------------------------------
    // OAuth2 fields
    // ----------------------------------------------------------------
    public static final int OAUTH_PROVIDER_MAX = 50;
    public static final int OAUTH_ID_MAX       = 255;
}

